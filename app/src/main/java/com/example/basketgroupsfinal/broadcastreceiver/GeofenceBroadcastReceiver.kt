package com.example.basketgroupsfinal.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.basketgroupsfinal.firebase.FirestoreClass
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        Log.d("BroadcastReceiver", "Intent action: ${intent.action}, extras: ${intent.extras}")

        intent.extras?.let {
            for (key in it.keySet()) {
                val value = it.get(key)
                Log.d("BroadcastReceiver", "Key: $key Value: $value")
            }
        }

        val geofencingEvent = try {
            GeofencingEvent.fromIntent(intent)
        } catch (e: IllegalArgumentException) {
            Log.e("BroadcastReceiver", "Invalid geofence event detected", e)
            null
        }

        geofencingEvent?.let { event ->
            if (event.hasError()) {
                val errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(event.errorCode)
                Log.e("BroadcastReceiver", errorMessage)
                return
            }

            when (event.geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> {
                    Log.d("BroadcastReceiver", "Geofence ENTER")
                }

                Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    Log.d("BroadcastReceiver", "Geofence EXIT")
                    event.triggeringGeofences?.let { triggeringGeofences ->
                        for (geofence in triggeringGeofences) {
                            val placeId = geofence.requestId
                            val userId = FirestoreClass().getCurrentUserID()
                            if(userId != ""){
                                FirestoreClass().removePlayerFromPlace(placeId, userId)
                            } else {
                                Log.d("BroadcastReceiver", "Geofence EXIT but no user")
                            }
                        }
                    }
                }

                Geofence.GEOFENCE_TRANSITION_DWELL -> {
                    Log.d("BroadcastReceiver", "Geofence DWELL")
                    event.triggeringGeofences?.let { triggeringGeofences ->
                        for (geofence in triggeringGeofences) {
                            val placeId = geofence.requestId
                            val userId = FirestoreClass().getCurrentUserID()
                            if(userId != ""){
                                FirestoreClass().addPlayerToPlace(placeId, userId)
                            } else {
                                Log.d("BroadcastReceiver", "Geofence DWELL but no user")
                            }
                        }
                    }
                }

                else -> {
                    Log.d("BroadcastReceiver", "Invalid Type")
                }
            }
        } ?: Log.d("BroadcastReceiver", "Geofence event is null")
    }

}