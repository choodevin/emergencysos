package com.emergency.sosalert.firebaseMessaging

data class PushNotification(
    val data: NotificationData,
    val to: String
)