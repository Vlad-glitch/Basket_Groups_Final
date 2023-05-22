package com.example.basketgroupsfinal.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.basketgroupsfinal.R
import com.example.basketgroupsfinal.models.PlacesViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class MapPlacesFragment: Fragment() {

    private lateinit var placesViewModel: PlacesViewModel
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_places_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync { map ->
            googleMap = map
            setupMapMarkers()

            Dexter.withContext(requireContext())
                .withPermissions(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .withListener(object : MultiplePermissionsListener {
                    @SuppressLint("MissingPermission")
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            googleMap.isMyLocationEnabled = true
                            getLastKnownLocation()
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

        placesViewModel = ViewModelProvider(requireActivity()).get(PlacesViewModel::class.java)
    }

    private fun setupMapMarkers() {
        placesViewModel.places.observe(viewLifecycleOwner, Observer { places ->
            places?.let {
                for (place in it) {
                    val location = LatLng(place.latitude, place.longitude)
                    googleMap.addMarker(MarkerOptions().position(location).title(place.title))
                }
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                } ?: run {
                    placesViewModel.places.value?.let { places ->
                        if (places.isNotEmpty()) {
                            val firstPlace = places[0]
                            val firstPlaceLatLng = LatLng(firstPlace.latitude, firstPlace.longitude)
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstPlaceLatLng, 12f))
                        } else {
                            // If no places and no location is available, set to a default location
                            val defaultLocation = LatLng(26.0322107, 26.0322107)
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("MapPlacesFragment", "Error trying to get last GPS location", e)
                // Zoom to a default location
                val defaultLocation = LatLng(44.4200644, 26.0322107)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
            }
    }

    private fun showRationalDialogForPermissions() {
        // This method is called when the user has previously denied one or more permissions and you need to explain why you need them.
        // You can show a dialog here and when the user click 'ok', call the method 'token?.continuePermissionRequest()'
    }
}