package com.emergency.sosalert.chat

import android.os.Parcel
import android.os.Parcelable

class ChatList() : Parcelable {
    var chatgroupuid: String = ""
    var target: String = ""

    constructor(parcel: Parcel) : this() {
        chatgroupuid = parcel.readString()!!
        target = parcel.readString()!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(chatgroupuid)
        parcel.writeString(target)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChatList> {
        override fun createFromParcel(parcel: Parcel): ChatList {
            return ChatList(parcel)
        }

        override fun newArray(size: Int): Array<ChatList?> {
            return arrayOfNulls(size)
        }
    }
}