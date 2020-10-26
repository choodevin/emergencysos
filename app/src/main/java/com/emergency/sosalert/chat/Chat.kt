package com.emergency.sosalert.chat

import android.os.Parcel
import android.os.Parcelable

class Chat() : Parcelable {
    var message: String = ""
    var sender: String = ""
    var timestamp: Long = 0

    constructor(parcel: Parcel) : this() {
        message = parcel.readString().toString()
        sender = parcel.readString().toString()
        timestamp = parcel.readLong()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(message)
        parcel.writeString(sender)
        parcel.writeLong(timestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Chat> {
        override fun createFromParcel(parcel: Parcel): Chat {
            return Chat(parcel)
        }

        override fun newArray(size: Int): Array<Chat?> {
            return arrayOfNulls(size)
        }
    }
}