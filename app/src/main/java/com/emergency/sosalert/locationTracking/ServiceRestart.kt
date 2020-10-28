package com.emergency.sosalert.locationTracking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build


class ServiceRestart : BroadcastReceiver() {
    override fun onReceive(context: Context?, p1: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.startForegroundService(Intent(context, LocationTrackingService::class.java))
        } else {
            context?.startService(Intent(context, LocationTrackingService::class.java))
        }
    }
}