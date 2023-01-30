package com.example.basketgroupsfinal.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.basketgroupsfinal.R
import com.example.basketgroupsfinal.databinding.ActivityMyProfileBinding
import com.example.basketgroupsfinal.databinding.ActivityPlaceDetailsBinding
import com.example.basketgroupsfinal.firebase.FirestoreClass
import com.example.basketgroupsfinal.models.Place
import com.example.basketgroupsfinal.utils.Constants

class PlaceDetailsActivity : BaseActivity() {

    private var binding: ActivityPlaceDetailsBinding? = null

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

        toolbarPlaceDetailsActivity?.setNavigationOnClickListener { onBackPressed() }

    }

    fun placeDetails(place: Place){
        hideProgressDialog()
        setupActionBar(place.title)

    }

}