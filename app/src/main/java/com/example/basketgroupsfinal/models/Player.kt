package com.example.basketgroupsfinal.models

import android.os.Parcel
import android.os.Parcelable

data class Player(
    var id: String? = "",
    var scheduledTime: Long = 0
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readLong()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeLong(scheduledTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Player> {
        override fun createFromParcel(parcel: Parcel): Player {
            return Player(parcel)
        }

        override fun newArray(size: Int): Array<Player?> {
            return arrayOfNulls(size)
        }
    }
}
