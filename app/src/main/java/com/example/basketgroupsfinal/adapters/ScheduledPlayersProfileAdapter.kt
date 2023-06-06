package com.example.basketgroupsfinal.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.basketgroupsfinal.R
import com.example.basketgroupsfinal.activities.ScheduledUser
import com.example.basketgroupsfinal.models.Player
import com.example.basketgroupsfinal.models.User
import java.text.DateFormat
import java.util.Date

class ScheduledPlayersProfileAdapter(
    private val context: Context,
    private val scheduledPlayerList: ArrayList<ScheduledUser>
) : RecyclerView.Adapter<ScheduledPlayersProfileAdapter.ScheduledPlayerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduledPlayerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_user_place, parent, false)
        return ScheduledPlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduledPlayerViewHolder, position: Int) {
        val scheduledPlayer = scheduledPlayerList[position]
        holder.bind(scheduledPlayer.user, scheduledPlayer.scheduledTime)
    }

    override fun getItemCount(): Int {
        return scheduledPlayerList.size
    }

    inner class ScheduledPlayerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val playerName: TextView = view.findViewById(R.id.tvName)
        private val playerImage: ImageView = view.findViewById(R.id.iv_user_image)
        private val playerTime: TextView = view.findViewById(R.id.tvTime)

        fun bind(user: User, scheduledTime: Long) {
            playerName.text = user.name
            playerTime.text = DateFormat.getDateTimeInstance().format(Date(scheduledTime))
            Glide
                .with(context)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_sports_basketball_24)
                .into(playerImage)
        }
    }
}
