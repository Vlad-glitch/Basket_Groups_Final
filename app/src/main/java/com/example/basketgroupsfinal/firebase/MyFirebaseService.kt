package com.example.basketgroupsfinal.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseService: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Get the current user
        val currentUser = FirestoreClass().getCurrentUserID()

        if (currentUser != null) {
            // Create a reference to the user document in Firestore
            val userRef = FirebaseFirestore.getInstance().collection("users").document(currentUser)

            // Set the new FCM token
            userRef.update("fcmToken", token)
        } else {
            // Save the token to shared preferences if there's no logged-in user
            val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("fcmToken", token)
                apply()
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // This method is called when a message is received.
        // Handle your message here. For example, if your app is in the foreground
        // you might want to show a notification to the user.
    }
}