package com.emergency.sosalert.discussion

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp

class Discussion() : Parcelable {
    var title: String = ""
    var description: String = ""
    var imageUrl: String = ""
    var uploadtime: Timestamp = Timestamp.now()
    var ownerUid: String = ""
    var latitude: String = ""
    var longitude: String = ""

    constructor(parcel: Parcel) : this() {
        title = parcel.readString()!!
        description = parcel.readString()!!
        imageUrl = parcel.readString()!!
        uploadtime = parcel.readParcelable(Timestamp::class.java.classLoader)!!
        ownerUid = parcel.readString()!!
        latitude = parcel.readString()!!
        longitude = parcel.readString()!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(imageUrl)
        parcel.writeParcelable(uploadtime, flags)
        parcel.writeString(ownerUid)
        parcel.writeString(latitude)
        parcel.writeString(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Discussion> {
        override fun createFromParcel(parcel: Parcel): Discussion {
            return Discussion(parcel)
        }

        override fun newArray(size: Int): Array<Discussion?> {
            return arrayOfNulls(size)
        }
    }
}