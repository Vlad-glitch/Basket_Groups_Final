package com.example.basketgroupsfinal.activities

import android.os.Bundle
import com.example.basketgroupsfinal.R
import com.example.basketgroupsfinal.databinding.ActivityMapBinding
import com.example.basketgroupsfinal.databinding.ActivityMyProfileBinding
import com.example.basketgroupsfinal.databinding.ActivityPlaceDetailsBinding
import com.example.basketgroupsfinal.firebase.FirestoreClass
import com.example.basketgroupsfinal.models.Place
import com.example.basketgroupsfinal.utils.Constants
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : BaseActivity(), OnMapReadyCallback  {
    private var binding: ActivityMapBinding? = null

    private var longitude: Double = 0.0
    private var latitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if(intent.hasExtra(Constants.LONGITUDE)){
            longitude = intent.getDoubleExtra(Constants.LONGITUDE, 0.0)
        }
        if(intent.hasExtra(Constants.LATITUDE)){
            latitude = intent.getDoubleExtra(Constants.LATITUDE, 0.0)
        }
        //showProgressDialog(resources.getString(R.string.please_wait))
        setupActionBar("Map")

        val supportMapFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync(this)

    }

    private fun setupActionBar(title: String) {

        val toolbarMap = binding?.toolbarMap

        setSupportActionBar(toolbarMap)

        val actionBar = supportActionBar

        if (actionBar != null) {
            //actionBar.setDisplayHomeAsUpEnabled(true)
            //actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = title
        }



        //toolbarMap?.setNavigationOnClickListener { onBackPressed() }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        /**
         * Add a marker on the location using the latitude and longitude and move the camera to it.
         */
        val position = LatLng(
            latitude,
            longitude
        )
        googleMap.addMarker(MarkerOptions().position(position).title("TestTitle"))
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 15f)
        googleMap.animateCamera(newLatLngZoom)

    }
}