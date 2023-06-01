package com.example.basketgroupsfinal.adapters

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.basketgroupsfinal.R
import com.example.basketgroupsfinal.models.User

class PlayersProfileAdapter(
    private val context: Context,
    private val playerList: ArrayList<User>
) : RecyclerView.Adapter<PlayersProfileAdapter.PlayerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_user_place, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = playerList[position]
        holder.bind(player)
    }

    override fun getItemCount(): Int {
        return playerList.size
    }

    inner class PlayerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val playerName: TextView = view.findViewById(R.id.tvName)
        private val playerImage: ImageView = view.findViewById(R.id.iv_user_image)

        fun bind(user: User) {
            playerName.text = user.name
            Glide
                .with(context)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_sports_basketball_24)
                .into(playerImage)
        }
    }
}