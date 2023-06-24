package com.example.basketgroupsfinal.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.basketgroupsfinal.R
import com.example.basketgroupsfinal.adapters.BasketPlacesAdapter
import com.example.basketgroupsfinal.broadcastreceiver.GeofenceBroadcastReceiver
import com.example.basketgroupsfinal.databinding.ActivityMainBinding
import com.example.basketgroupsfinal.firebase.FirestoreClass
import com.example.basketgroupsfinal.models.Place
import com.example.basketgroupsfinal.models.PlacesViewModel
import com.example.basketgroupsfinal.models.User
import com.example.basketgroupsfinal.utils.Constants
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.gms.location.GeofencingClient
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, NavigationBarView.OnItemSelectedListener{

    private var binding: ActivityMainBinding? = null
    private lateinit var placesViewModel: PlacesViewModel
    private lateinit var geofencingClient: GeofencingClient
    private val addedGeofences = mutableSetOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        geofencingClient = LocationServices.getGeofencingClient(application.applicationContext)

        setupActionBar()

        binding!!.navView.setNavigationItemSelectedListener(this)

        binding!!.appBarMain.bottomNav.setOnItemSelectedListener(this)

        placesViewModel = ViewModelProvider(this).get(PlacesViewModel::class.java)

        placesViewModel.loadPlaces()

        //requestPermissions()

        placesViewModel.places.observe(this) { places ->
            for (place in places) {
                if (addedGeofences.contains(place.id)) {
                    // This geofence has already been added, so skip it
                    continue
                }

                // This geofence has not been added yet, so add it
                addedGeofences.add(place.id)
                startGeofence(place.id, place.latitude, place.longitude, 100.0f)
            }
        }

        FirestoreClass().loadUserData(this@MainActivity)

    }

    private fun setPendingIntent(geoId: String, action: String): PendingIntent {
        val intent = Intent(application, GeofenceBroadcastReceiver::class.java)
        intent.action = action
        val pendingIntentId = geoId.hashCode() // Generate an Int ID from the String ID

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                application,
                pendingIntentId,
                intent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getBroadcast(
                application,
                pendingIntentId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    fun startGeofence(
        id: String,
        latitude: Double,
        longitude: Double,
        geoRadius: Float
    ) {
        Log.d("MainActivity", "Start geofence for place: $id")

        Dexter.withContext(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(report: PermissionGrantedResponse) {
                    // Fine location permission is granted
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // If API level is 29 or above, ask for background location permission.
                        Dexter.withContext(this@MainActivity)
                            .withPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            .withListener(object : PermissionListener {
                                override fun onPermissionGranted(report: PermissionGrantedResponse) {
                                    addGeofence(id, latitude, longitude, geoRadius) // call the separated geofencing method here
                                }

                                override fun onPermissionDenied(report: PermissionDeniedResponse) {
                                    // Handle denied background location permission
                                    Log.d("Geofence", "Background location permission not granted.")
                                }

                                override fun onPermissionRationaleShouldBeShown(
                                    permission: PermissionRequest?,
                                    token: PermissionToken?
                                ) {
                                    // This method is called when the user denies a permission
                                    // and the permission rationale should be shown.
                                    token?.continuePermissionRequest()
                                }
                            }).check()
                    } else {
                        // If API level is lower than 29, we're ready to start geofencing without asking for background location permission.
                        addGeofence(id, latitude, longitude, geoRadius) // call the separated geofencing method here
                    }
                }

                override fun onPermissionDenied(report: PermissionDeniedResponse) {
                    // Handle denied fine location permission
                    Log.d("Geofence", "Fine location permission not granted.")
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    // This method is called when the user denies a permission
                    // and the permission rationale should be shown.
                    token?.continuePermissionRequest()
                }

            }).check()
    }

    @SuppressLint("MissingPermission")
    fun addGeofence(
        id: String,
        latitude: Double,
        longitude: Double,
        geoRadius: Float
    ) {
        // The geofencing code is moved here.
        val geofence = Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(
                latitude,
                longitude,
                geoRadius
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER
                        or Geofence.GEOFENCE_TRANSITION_EXIT
                        or Geofence.GEOFENCE_TRANSITION_DWELL
            )
            .setLoiteringDelay(5000) // Time between GEOFENCE_TRANSITION_ENTER and GEOFENCE_TRANSITION_DWELL
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(
                GeofencingRequest.INITIAL_TRIGGER_ENTER
                        or GeofencingRequest.INITIAL_TRIGGER_EXIT
                        or GeofencingRequest.INITIAL_TRIGGER_DWELL
            )
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, setPendingIntent(id, "com.example.basketgroupsfinal.ACTION_GEOFENCE_EVENT"))
            .run {
                addOnSuccessListener {
                    Log.d("Geofence", "Successfully added. ID: $id, Lat: $latitude, Long: $longitude, Radius: $geoRadius")

                }
                addOnFailureListener {
                    Log.e("Geofence", it.message.toString() + " Geofence not added")
                }
            }

    }

    private fun setupActionBar() {

        val toolbarMainActivity = binding?.appBarMain?.toolbarMainActivity

        setSupportActionBar(toolbarMainActivity)
        toolbarMainActivity?.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbarMainActivity?.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer() {
        val drawerLayout = binding?.drawerLayout

        if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                FirestoreClass().loadUserData(this@MainActivity)
            } else {
                Log.e("Cancelled", "Cancelled")
            }
        }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val drawerLayout = binding?.drawerLayout

        when (item.itemId) {
            R.id.nav_my_profile -> {
                intent = Intent(this, MyProfileActivity::class.java)
                //startActivity(Intent(this, MyProfileActivity::class.java))
                getResult.launch(intent)
            }

            R.id.nav_sign_out -> {

                // Here sign outs the user from firebase in this device.
                FirebaseAuth.getInstance().signOut()

                // Send the user to the intro screen of the application.
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()

            }

            R.id.nav_list -> {
                supportFragmentManager.commit {
                    replace(R.id.fragment_content, PlaceListFragment())
                }
            }

            R.id.nav_map -> {
                supportFragmentManager.commit {
                    replace(R.id.fragment_content, MapPlacesFragment())
                }
            }
        }
        drawerLayout?.closeDrawer(GravityCompat.START)
        // END
        return true
    }

    fun updateNavigationUserDetails(loggedInUser: User) {

        // The instance of the header view of the navigation view.
        val headerView = binding?.navView?.getHeaderView(0)

        // The instance of the user image of the navigation view.
        val navUserImage = headerView!!.findViewById<ImageView>(R.id.iv_user_image)

        // Load the user image in the ImageView.
        Glide
            .with(this@MainActivity)
            .load(loggedInUser.image) // URL of the image
            .centerCrop() // Scale type of the image.
            .placeholder(R.drawable.ic_user_place_holder) // A default place holder
            .into(navUserImage) // the view in which the image will be loaded.

        // The instance of the user name TextView of the navigation view.
        val navUsername = headerView.findViewById<TextView>(R.id.tv_username)
        // Set the user name
        navUsername.text = loggedInUser.name

        //showProgressDialog(resources.getString(R.string.please_wait))
    }

}