package com.emergency.sosalert.chat

import com.google.firebase.Timestamp

class Chat() {
    var message: String = ""
    var owner: String = ""
    lateinit var timestamp: Timestamp
}