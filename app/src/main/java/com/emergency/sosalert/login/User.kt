package com.emergency.sosalert.login

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable
import java.util.*

class User() : Parcelable {
    var uid: String = ""
    var name: String = ""
    var gender: String = ""
    var dob: String = ""
    var contact: String = ""

    constructor(parcel: Parcel) : this() {
        uid = parcel.readString()!!
        name = parcel.readString()!!
        gender = parcel.readString()!!
        dob = parcel.readString()!!
        contact = parcel.readString()!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(name)
        parcel.writeString(gender)
        parcel.writeString(dob)
        parcel.writeString(contact)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}
