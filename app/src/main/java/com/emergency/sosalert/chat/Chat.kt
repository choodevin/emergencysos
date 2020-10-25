package com.emergency.sosalert.chat

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

class Chat() : Parcelable {
    var message: String = ""
    var owner: String = ""
    lateinit var timestamp: Timestamp

    constructor(parcel: Parcel) : this() {
        message = parcel.readString().toString()
        owner = parcel.readString().toString()
        timestamp = parcel.readParcelable(Timestamp::class.java.classLoader)!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(message)
        parcel.writeString(owner)
        parcel.writeParcelable(timestamp, flags)
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