package com.example.basketgroupsfinal.activities

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.basketgroupsfinal.R
import com.example.basketgroupsfinal.adapters.PlayersProfileAdapter
import com.example.basketgroupsfinal.adapters.ScheduledPlayersProfileAdapter
import com.example.basketgroupsfinal.broadcastreceiver.PlayerJoinAlarmReceiver
import com.example.basketgroupsfinal.databinding.ActivityPlaceDetailsBinding
import com.example.basketgroupsfinal.firebase.FirestoreClass
import com.example.basketgroupsfinal.models.Place
import com.example.basketgroupsfinal.models.Player
import com.example.basketgroupsfinal.models.User
import com.example.basketgroupsfinal.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

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
        binding?.btnSchedule?.setOnClickListener {

            // Create the date and time picker dialog
            val dateTimePickerDialog = Dialog(this)
            dateTimePickerDialog.setContentView(R.layout.dialog_date_time_picker)

            val datePicker = dateTimePickerDialog.findViewById<DatePicker>(R.id.date_picker)
            val timePicker = dateTimePickerDialog.findViewById<TimePicker>(R.id.time_picker)

            // Set a click listener for the "OK" button
            dateTimePickerDialog.findViewById<Button>(R.id.btn_ok).setOnClickListener {
                val day = datePicker.dayOfMonth
                val month = datePicker.month
                val year = datePicker.year

                val hour = timePicker.hour
                val minute = timePicker.minute

                // Convert the selected date and time into milliseconds
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day, hour, minute)
                val scheduledTime = calendar.timeInMillis

                // Check if the selected time has already passed
                if (scheduledTime < System.currentTimeMillis()) {
                    // If the selected time has already passed, show a toast message and return
                    Toast.makeText(this, "You cannot schedule yourself for a past time", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Create a new Player object with the current user's ID and the scheduled time
                val player = Player(FirebaseAuth.getInstance().currentUser!!.uid, scheduledTime)

                // Find the existing player in the list, if they exist
                val existingPlayer = currentPlace?.scheduledPlayers?.find { it.id == player.id }

                if (existingPlayer != null) {
                    // The player is already in the list, so update their scheduled time
                    existingPlayer.scheduledTime = player.scheduledTime
                } else {
                    // The player is not in the list, so add them
                    currentPlace?.scheduledPlayers?.add(player)
                }

                // Update the place in the Firestore database
                mFireStore.collection(Constants.PLACE).document(currentPlace!!.id)
                    .update("scheduledPlayers", currentPlace?.scheduledPlayers)

                // Update the UI to reflect the new list of scheduled players
                updateScheduledPlayers(currentPlace!!.scheduledPlayers)

                // Prepare the intent for the alarm
                val alarmIntent = Intent(this, PlayerJoinAlarmReceiver::class.java).apply {
                    putExtra("playerId", player.id)
                    putExtra("placeId", currentPlace?.id)
                }

                // Generate a unique request code
                val requestCode = (player.id + scheduledTime.toString()).hashCode()

                // Create the PendingIntent
                val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    requestCode,
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                            PendingIntent.FLAG_IMMUTABLE
                )

                // Set the alarm to trigger at the scheduled time
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduledTime, pendingIntent)

                dateTimePickerDialog.dismiss()
            }

            dateTimePickerDialog.show()
        }
    }

    private fun updateScheduledPlayers(scheduledPlayers: ArrayList<Player>) {
        // create an empty list to hold the user objects
        val players: MutableList<ScheduledUser> = mutableListOf()

        // iterate over the player objects and fetch each user
        scheduledPlayers.forEach { player ->
            player.id?.let {
                mFireStore.collection(Constants.USERS).document(it)
                    .get()
                    .addOnSuccessListener { document ->
                        val user = document.toObject(User::class.java)
                        user?.let {
                            players.add(ScheduledUser(it, player.scheduledTime))

                            // Check if all users are fetched
                            if(players.size == scheduledPlayers.size) {
                                binding?.tvNumberScheduled?.text = players.size.toString()
                                // Now you have all the user objects. Update your RecyclerView here
                                val playerAdapter = ScheduledPlayersProfileAdapter(
                                    this,
                                    players as ArrayList<ScheduledUser>
                                )
                                binding?.rvScheduledPlayersList?.layoutManager = LinearLayoutManager(this)
                                binding?.rvScheduledPlayersList?.adapter = playerAdapter
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w("FirestoreClass", "Error getting user", e)
                    }
            }
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
                    updatePlayers(it.players)  // Call a method that updates the TextView and the RecyclerView for Players
                    updateScheduledPlayers(it.scheduledPlayers)  // Call a method that updates the RecyclerView for ScheduledPlayers
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
                            binding?.rvCurrentPlayersList?.layoutManager = LinearLayoutManager(this)
                            binding?.rvCurrentPlayersList?.adapter = playerAdapter
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("FirestoreClass", "Error getting user", e)
                }
        }
    }

}

data class ScheduledUser(val user: User, val scheduledTime: Long)
