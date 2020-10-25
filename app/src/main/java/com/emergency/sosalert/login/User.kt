package com.emergency.sosalert.login

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class User() : Parcelable {
    var uid: String = ""
    var name: String = ""
    var gender: String = ""
    var age: Int = 0

    constructor(parcel: Parcel) : this() {
        uid = parcel.readString()!!
        name = parcel.readString()!!
        gender = parcel.readString()!!
        age = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(name)
        parcel.writeString(gender)
        parcel.writeInt(age)
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
