package com.emergency.sosalert.firebaseMessaging

import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat

class SOSRejectReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        val data = p1?.extras!!.get("reject").toString() //get data like this
        Log.e(TAG, "reject received $data")

        p0.apply {
            this?.let {
                NotificationManagerCompat.from(it).cancel(p1.extras!!.getInt("notificationid"))
            }
        }
    }

}