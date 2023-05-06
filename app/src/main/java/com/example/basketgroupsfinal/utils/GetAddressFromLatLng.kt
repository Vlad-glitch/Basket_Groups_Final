package com.example.basketgroupsfinal.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*


// TODO(Step 1: Create a AsyncTask class fot getting an address from the latitude and longitude from the location provider.)
// START
/**
 * A AsyncTask class to get the address from latitude and longitude.
 */
class GetAddressFromLatLng(
    context: Context,
    private val latitude: Double,
    private val longitude: Double
) {
    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
    private lateinit var mAddressListener: AddressListener

    fun getAddress(scope: CoroutineScope = CoroutineScope(Dispatchers.Main)) {
        scope.launch {
            val resultString = withContext(Dispatchers.IO) { fetchAddress() }
            if (resultString == null) {
                mAddressListener.onError()
            } else {
                mAddressListener.onAddressFound(resultString)
            }
        }
    }

    private fun fetchAddress(): String? {
        return try {
            val addressList: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (addressList != null && addressList.isNotEmpty()) {
                val address: Address = addressList[0]
                val sb = StringBuilder()
                for (i in 0..address.maxAddressLineIndex) {
                    sb.append(address.getAddressLine(i)).append(",")
                }
                sb.deleteCharAt(sb.length - 1)
                sb.toString()
            } else {
                null
            }
        } catch (e: IOException) {
            null
        }
    }

    fun setAddressListener(addressListener: AddressListener) {
        mAddressListener = addressListener
    }

    interface AddressListener {
        fun onAddressFound(address: String?)
        fun onError()
    }
}