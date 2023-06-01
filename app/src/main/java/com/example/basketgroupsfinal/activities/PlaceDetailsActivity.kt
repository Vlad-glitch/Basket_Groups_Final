package com.example.basketgroupsfinal.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.basketgroupsfinal.R
import com.example.basketgroupsfinal.adapters.PlayersProfileAdapter
import com.example.basketgroupsfinal.databinding.ActivityPlaceDetailsBinding
import com.example.basketgroupsfinal.firebase.FirestoreClass
import com.example.basketgroupsfinal.models.Place
import com.example.basketgroupsfinal.models.User
import com.example.basketgroupsfinal.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore

class PlaceDetailsActivity : BaseActivity() {

    private var binding: ActivityPlaceDetailsBinding? = null

    private var currentPlace: Place? = null

    private val mFireStore = FirebaseFirestore.getInstance()

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

        listenForUpdates(placeDocumentId)

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

        binding?.tvTitle?.text = place.title
        binding?.tvDescription?.text = place.description


        val ivImage = binding?.ivPlaceImage

        Glide
            .with(this@PlaceDetailsActivity)
            .load(place.image)
            .centerCrop()
            .placeholder(R.drawable.detail_screen_image_placeholder)
            .into(ivImage!!)

    }

    private fun listenForUpdates(placeDocumentId: String) {
        val docRef = mFireStore.collection(Constants.PLACE).document(placeDocumentId)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("FirestoreClass", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val place = snapshot.toObject(Place::class.java)
                place?.let {
                    updatePlayers(it.players)  // Call a method that updates the TextView and the RecyclerView
                }
            } else {
                Log.d("FirestoreClass", "Current data: null")
            }
        }
    }

    private fun updatePlayers(playerIds: List<String>) {
        // create an empty list to hold the user objects
        val players: MutableList<User> = mutableListOf()

        // iterate over the IDs and fetch each user
        playerIds.forEach { id ->
            mFireStore.collection(Constants.USERS).document(id)
                .get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(User::class.java)
                    user?.let {
                        players.add(it)

                        // Check if all users are fetched
                        if(players.size == playerIds.size) {
                            // Now you have all the user objects. Update your RecyclerView here
                            binding?.tvNumber?.text = players.size.toString()

                            val playerAdapter = PlayersProfileAdapter(this,
                                players as ArrayList<User>
                            )
                            binding?.rvPlayersList?.layoutManager = LinearLayoutManager(this)
                            binding?.rvPlayersList?.adapter = playerAdapter
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("FirestoreClass", "Error getting user", e)
                }
        }
    }

}