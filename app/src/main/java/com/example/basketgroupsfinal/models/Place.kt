package com.example.basketgroupsfinal.models

import android.os.Parcel
import android.os.Parcelable


data class Place(
    var id: String = "default",
    var title: String = "default",
    var image: String = "default",
    var description: String = "default",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var players: ArrayList<String> = ArrayList(),
    var scheduledPlayers: ArrayList<Player> = ArrayList()
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.createStringArrayList()!!,
        parcel.readArrayList(Player::class.java.classLoader) as ArrayList<Player>

    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(image)
        parcel.writeString(description)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeStringList(players)
        parcel.writeList(scheduledPlayers)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Place> {
        override fun createFromParcel(parcel: Parcel): Place {
            return Place(parcel)
        }

        override fun newArray(size: Int): Array<Place?> {
            return arrayOfNulls(size)
        }
    }
}