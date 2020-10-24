package com.emergency.sosalert.firebaseMessaging

data class NotificationData(
    val title: String,
    val message: String,
    val latitude: String,
    val longitude: String,
    val image: String
)