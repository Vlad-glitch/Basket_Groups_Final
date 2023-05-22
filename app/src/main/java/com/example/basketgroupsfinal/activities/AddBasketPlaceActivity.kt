package com.example.basketgroupsfinal.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.basketgroupsfinal.R
import com.example.basketgroupsfinal.databinding.ActivityAddBasketPlaceBinding
import com.example.basketgroupsfinal.firebase.FirestoreClass
import com.example.basketgroupsfinal.models.Place
import com.example.basketgroupsfinal.utils.GetAddressFromLatLng
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.IOException
import java.util.*


class AddBasketPlaceActivity : BaseActivity() {

    private var binding: ActivityAddBasketPlaceBinding? = null

    private var mSelectedImageFileUri: Uri? = null

    private var mPlaceImgURL : String = ""

    private var mLatitude: Double = 0.0 // A variable which will hold the latitude value.
    private var mLongitude: Double = 0.0 // A variable which will hold the longitude value.

    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBasketPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupActionBar()

        /*this@AddBasketPlaceActivity.packageManager.getApplicationInfo(this@AddBasketPlaceActivity.packageName, PackageManager.GET_META_DATA).apply {
            val apiKey = metaData.getString("com.google.android.geo.API_KEY")
        }

        if(!Places.isInitialized()){
            Places.initialize(this@AddBasketPlaceActivity, apiKey)
            //Places.initialize(this@AddBasketPlaceActivity, )
            val placesClient = Places.createClient(this)

        }
         */

        val apiKey = getMapsApiKeyFromManifest()

        if(!Places.isInitialized()){
            Places.initialize(this@AddBasketPlaceActivity, apiKey)
            //Places.initialize(this@AddBasketPlaceActivity, )
            val placesClient = Places.createClient(this)

        }


        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Handle the result
                val data = result.data
                if (data != null) {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    binding?.etLocation?.setText(place.address)
                    mLatitude = place.latLng!!.latitude
                    mLongitude = place.latLng!!.longitude
                }

            } else if (result.resultCode == AutocompleteActivity.RESULT_ERROR) {
                // Handle any errors
                val data = result.data
                if (data != null) {
                    val status = Autocomplete.getStatusFromIntent(data)
                    Log.e(TAG, status.statusMessage!!)
                }
            }
        }

        binding?.etLocation?.setOnClickListener {
            try {
                // These are the list of fields which we required is passed
                val fields = listOf(
                    com.google.android.libraries.places.api.model.Place.Field.ID,
                    com.google.android.libraries.places.api.model.Place.Field.NAME,
                    com.google.android.libraries.places.api.model.Place.Field.LAT_LNG,
                    com.google.android.libraries.places.api.model.Place.Field.ADDRESS
                )
                // Start the autocomplete intent with a unique request code.
                val intent =
                    Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(this@AddBasketPlaceActivity)
                launcher.launch(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        binding?.tvAddImage?.setOnClickListener {
            choosePhotoFromGal()
        }

        binding?.tvSelectCurrentLocation?.setOnClickListener {
            if (!isLocationEnabled()) {
                Toast.makeText(
                    this,
                    "Your location provider is turned off. Please turn it on.",
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            } else {
                Dexter.withContext(this)
                    .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if (report!!.areAllPermissionsGranted()) {

                                requestNewLocationData()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            showRationalDialogForPermissions()
                        }
                    }).onSameThread()
                    .check()

            }
        }

        binding?.btnSave?.setOnClickListener {
            //TODO
            when {
                binding?.etTitle?.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()
                }
                binding?.etDescription?.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT)
                        .show()
                }
                binding?.etLocation?.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "Please select location", Toast.LENGTH_SHORT)
                        .show()
                }
                mSelectedImageFileUri == null -> {
                    Toast.makeText(this, "Please add image", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    uploadPlaceImg()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {

        val mLocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(500)
            .setMaxUpdateDelayMillis(1000)
            .setMaxUpdates(1)
            .build()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation
            if (mLastLocation != null) {
                mLatitude = mLastLocation.latitude
            }
            if (mLastLocation != null) {
                mLongitude = mLastLocation.longitude
            }
            val addressTask = GetAddressFromLatLng(this@AddBasketPlaceActivity, mLatitude, mLongitude)

            addressTask.setAddressListener(object :
                GetAddressFromLatLng.AddressListener {
                override fun onAddressFound(address: String?) {
                    Log.i("Address ::", "" + address)
                    binding?.etLocation?.setText(address) // Address is set to the edittext
                }

                override fun onError() {
                    Log.e("Get Address ::", "Something is wrong...")
                }
            })

            addressTask.getAddress()
        }
    }



    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun createPlace() {

        var place = Place(title = binding?.etTitle?.text.toString(),
            description = binding?.etDescription?.text.toString(),
            image = mPlaceImgURL,
            latitude = mLatitude, longitude = mLongitude
        )

        FirestoreClass().addPlace(this, place)
    }

    private fun uploadPlaceImg(){
        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageFileUri != null) {

            //getting the storage reference
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "PLACE_IMAGE" + System.currentTimeMillis() + "." + getFileExtension(
                    mSelectedImageFileUri
                )
            )

            //adding the file to reference
            sRef.putFile(mSelectedImageFileUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    // The image upload is success
                    Log.i(
                        "Place Image URL",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                    )

                    // Get the downloadable url from the task snapshot
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri ->
                            Log.i("Downloadable Image URL", uri.toString())

                            // assign the image url to the variable.
                            mPlaceImgURL = uri.toString()

                            // Call a function to update user details in the database.
                            createPlace()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        exception.message,
                        Toast.LENGTH_LONG
                    ).show()

                    hideProgressDialog()
                }
        }

    }

    private fun getFileExtension(uri: Uri?): String? {
        /*
         * MimeTypeMap: Two-way map that maps MIME-types to file extensions and vice versa.
         *
         * getSingleton(): Get the singleton instance of MimeTypeMap.
         *
         * getExtensionFromMimeType: Return the registered extension for the given MIME type.
         *
         * contentResolver.getType: Return the MIME type of the given content URL.
         */
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    fun placeCreatedSuccessfully() {

        hideProgressDialog()

        //finish()
    }


    private fun choosePhotoFromGal() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // TODO (Step 8: Call the image chooser function.)
            // START
            showImageChooser()
            // END
        } else {
            /*Requests permissions to be granted to this application. These permissions
             must be requested in your manifest, they should not be granted to your app,
             and they should have protection level*/
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                MyProfileActivity.READ_STORAGE_PERMISSION_CODE
            )
        }
    }

    private val getImage = registerForActivityResult(
        ActivityResultContracts.GetContent(),
        ActivityResultCallback {

            mSelectedImageFileUri = it

            try{
                Glide
                    .with(this@AddBasketPlaceActivity)
                    .load(it)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(binding?.ivPlaceImage!!)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    )

    private fun showImageChooser() {
        getImage.launch("image/*")
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarAddActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
            actionBar.title = resources.getString(R.string.add_place)
        }
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("It Looks like you have turned off permissions required for this feature. It can be enabled under Application Settings")
            .setPositiveButton(
                "GO TO SETTINGS"
            ) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog,
                                           _ ->
                dialog.dismiss()
            }.show()
    }

    private fun getMapsApiKeyFromManifest(): String {
        return try {
            val applicationInfo = this@AddBasketPlaceActivity.packageManager.getApplicationInfo(this@AddBasketPlaceActivity.packageName, PackageManager.GET_META_DATA)
            val metaData = applicationInfo.metaData

            // Add logging statements to check the state of the objects
            //Log.d("DEBUG", "applicationInfo: $applicationInfo")
            //Log.d("DEBUG", "metaData: $metaData")

            metaData?.getString("com.google.android.geo.API_KEY") ?: "not found"
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("DEBUG", "Error getting metadata", e)
            "not found"
        }
    }
}