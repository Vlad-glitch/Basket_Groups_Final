package com.example.basketgroupsfinal.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.basketgroupsfinal.firebase.FirestoreClass

class PlacesViewModel : ViewModel() {
    private val _places = MutableLiveData<List<Place>>()
    val places: LiveData<List<Place>> get() = _places

    init {
        loadPlaces()
    }

    fun loadPlaces() {
        FirestoreClass().getPlacesList(object : FirestoreListener {
            override fun onPlacesLoaded(places: List<Place>) {
                _places.value = places
            }
        })
    }
}

interface FirestoreListener{
    fun onPlacesLoaded(places: List<Place>)
}