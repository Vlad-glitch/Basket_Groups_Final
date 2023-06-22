package com.example.basketgroupsfinal.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.basketgroupsfinal.R
import com.example.basketgroupsfinal.adapters.ScheduledPlayersProfileAdapter
import com.example.basketgroupsfinal.databinding.ActivityScheduledPlayersCalendarBinding
import com.example.basketgroupsfinal.models.Player
import com.example.basketgroupsfinal.models.User
import com.example.basketgroupsfinal.utils.Constants
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class ScheduledPlayersCalendarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScheduledPlayersCalendarBinding
    private val mFireStore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduledPlayersCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val scheduledPlayers = intent.getParcelableArrayListExtra<Player>(Constants.SCHEDULED_PLAYERS) as ArrayList<Player>

        val playersByDate = scheduledPlayers.groupBy { player -> getDateAtStartOfDayInMillis(player.scheduledTime) }

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val selectedDateAtStartOfDayInMillis = getDateAtStartOfDayInMillis(calendar.timeInMillis)

            val players = playersByDate[selectedDateAtStartOfDayInMillis]
            if (players != null) {
                showPlayerDialog(players)
            } else {
                Toast.makeText(this, "No players scheduled for this day.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDateAtStartOfDayInMillis(dateInMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun showPlayerDialog(players: List<Player>) {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_scheduled_players, null)

        val scheduledUsers: MutableList<ScheduledUser> = mutableListOf()

        val playerIds = players.map { it.id }.filterNotNull()

        val fetchPlayersTasks = playerIds.map { playerId ->
            mFireStore.collection(Constants.USERS).document(playerId).get()
        }

        Tasks.whenAllSuccess<DocumentSnapshot>(fetchPlayersTasks)
            .addOnSuccessListener { documents ->
                documents.forEach { document ->
                    val user = document.toObject(User::class.java)
                    user?.let {
                        val player = players.firstOrNull { it.id == user.id }
                        player?.let {
                            scheduledUsers.add(ScheduledUser(user, player.scheduledTime))
                        }
                    }
                }

                val playerAdapter = ScheduledPlayersProfileAdapter(
                    this,
                    scheduledUsers as ArrayList<ScheduledUser>
                )
                dialogView.findViewById<RecyclerView>(R.id.rv_scheduled_players).apply {
                    layoutManager = LinearLayoutManager(this@ScheduledPlayersCalendarActivity)
                    adapter = playerAdapter
                }

                builder.setView(dialogView)
                val playerDialog = builder.create()
                playerDialog.show()
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreClass", "Error getting user", e)
            }
    }
}