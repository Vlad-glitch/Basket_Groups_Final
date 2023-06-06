package com.example.basketgroupsfinal.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.basketgroupsfinal.models.Place
import com.example.basketgroupsfinal.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore

class PlayerJoinAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Get the player ID and the place ID from the intent
        val playerId = intent.getStringExtra("playerId")
        val placeId = intent.getStringExtra("placeId")

        if (playerId != null && placeId != null) {
            // Get a Firestore instance
            val mFireStore = FirebaseFirestore.getInstance()

            // Start a transaction to update the Place document in Firestore
            mFireStore.runTransaction { transaction ->
                val placeRef = mFireStore.collection(Constants.PLACE).document(placeId)
                val place = transaction.get(placeRef).toObject(Place::class.java)

                place?.let {
                    // Find the scheduled player in the list
                    val scheduledPlayer =
                        it.scheduledPlayers.find { player -> player.id == playerId }

                    // If the player is found, remove them from the scheduledPlayers list
                    if (scheduledPlayer != null) {
                        it.scheduledPlayers.remove(scheduledPlayer)

                        // Update the Place document
                        transaction.update(placeRef, "scheduledPlayers", it.scheduledPlayers)
                    }
                }
            }
        }
    }
}
