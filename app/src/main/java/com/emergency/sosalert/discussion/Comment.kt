package com.emergency.sosalert.discussion

import android.os.Parcel
import android.os.Parcelable

class Comment() : Parcelable {
    var owner: String = ""
    var content: String = ""

    constructor(parcel: Parcel) : this() {
        owner = parcel.readString()!!
        content = parcel.readString()!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(owner)
        parcel.writeString(content)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Comment> {
        override fun createFromParcel(parcel: Parcel): Comment {
            return Comment(parcel)
        }

        override fun newArray(size: Int): Array<Comment?> {
            return arrayOfNulls(size)
        }
    }
}