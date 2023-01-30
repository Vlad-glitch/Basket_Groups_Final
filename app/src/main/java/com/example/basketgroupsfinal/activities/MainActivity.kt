package com.example.basketgroupsfinal.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.basketgroupsfinal.R
import com.example.basketgroupsfinal.adapters.BasketPlacesAdapter
import com.example.basketgroupsfinal.databinding.ActivityMainBinding
import com.example.basketgroupsfinal.firebase.FirestoreClass
import com.example.basketgroupsfinal.models.Place
import com.example.basketgroupsfinal.models.User
import com.example.basketgroupsfinal.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        binding!!.navView.setNavigationItemSelectedListener(this)

        FirestoreClass().loadUserData(this@MainActivity)

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

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getPlacesList(this)

    }

    fun setupBasketPlaces(basketPlaceList: ArrayList<Place>?){
        //TODO
        hideProgressDialog()
        val rvBasketPlaceList: RecyclerView = findViewById(R.id.rv_basket_place_list)
        if (basketPlaceList != null) {
            if (basketPlaceList.size > 0) {
                rvBasketPlaceList.visibility = View.VISIBLE
                rvBasketPlaceList.layoutManager = LinearLayoutManager(this)
                rvBasketPlaceList.setHasFixedSize(true)
                val adapter = BasketPlacesAdapter(this, basketPlaceList)
                rvBasketPlaceList.adapter = adapter

                adapter.setOnClickListener(object: BasketPlacesAdapter.OnClickListener {
                    override fun onClick(position: Int, model: Place) {
                        val intent = Intent(this@MainActivity, PlaceDetailsActivity::class.java)
                        intent.putExtra(Constants.DOCUMENT_ID, model.id)
                        startActivity(intent)
                    }
                })
            }else {
                rvBasketPlaceList.visibility = View.GONE
            }
        }

    }

}