package com.example.basketgroupsfinal.activities

import android.content.Intent
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.basketgroupsfinal.R
import com.example.basketgroupsfinal.databinding.ActivityPlaceDetailsBinding
import com.example.basketgroupsfinal.firebase.FirestoreClass
import com.example.basketgroupsfinal.models.Place
import com.example.basketgroupsfinal.utils.Constants

class PlaceDetailsActivity : BaseActivity() {

    private var binding: ActivityPlaceDetailsBinding? = null

    private var currentPlace: Place? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        var placeDocumentId = ""

        if(intent.hasExtra(Constants.DOCUMENT_ID)){
            placeDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getPlaceDetails(this, placeDocumentId)

        binding?.btnLocation?.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra(Constants.LATITUDE, currentPlace?.latitude)
            intent.putExtra(Constants.LONGITUDE, currentPlace?.longitude)
            startActivity(intent)
        }
    }

    private fun setupActionBar(title: String) {

        val toolbarPlaceDetailsActivity = binding?.toolbarPlaceDetailsActivity

        setSupportActionBar(toolbarPlaceDetailsActivity)

        val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = title
        }

        //toolbarPlaceDetailsActivity?.setNavigationOnClickListener { onBackPressed() }

    }

    fun placeDetails(place: Place){
        hideProgressDialog()
        currentPlace = place
        setupActionBar(place.title)

        val ivImage = binding?.ivPlaceImage

        Glide
            .with(this@PlaceDetailsActivity)
            .load(place.image)
            .centerCrop()
            .placeholder(R.drawable.detail_screen_image_placeholder)
            .into(ivImage!!)

    }

}