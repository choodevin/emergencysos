package com.emergency.sosalert.locationTracking

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.emergency.sosalert.MainActivity
import com.emergency.sosalert.R
import com.google.firebase.FirebaseApp


class LocationTrackingService : Service() {

    private lateinit var locationManager: LocationManager
    private var mLocationListeners = arrayOf(
        LocationListener(LocationManager.GPS_PROVIDER),
        LocationListener(LocationManager.NETWORK_PROVIDER)
    )
    private val LOCATION_INTERVAL: Long = 1000
    private val LOCATION_DISTANCE = 10f
    private val CHANNEL_ID: String = "9999"

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.e(TAG, "Service started")
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentText("The app is now running on background")
            .setSmallIcon(R.drawable.logo_sos)
            .setOngoing(true)
            .build()

        startForeground(CHANNEL_ID.toInt(), notification)

        Log.e(TAG, "Service created")

        initializeLocationManager()
        try {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                mLocationListeners[1]
            )
        } catch (ex: SecurityException) {
            Log.i(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "network provider does not exist, " + ex.message)
        }
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                mLocationListeners[0]
            )
        } catch (ex: SecurityException) {
            Log.i(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "gps provider does not exist " + ex.message)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val broadcastIntent = Intent()
        broadcastIntent.setAction("RestartLocationTrackingService")
            .setClass(this, ServiceRestart::class.java)
        sendBroadcast(broadcastIntent)
    }


    private fun initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager")
        locationManager =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    class LocationListener(provider: String) : android.location.LocationListener {
        var mLastLocation: Location
        override fun onLocationChanged(location: Location) {
            Log.e(TAG, "onLocationChanged: $location")
            mLastLocation.set(location)
            MainActivity.updateLocation(mLastLocation)
        }

        override fun onProviderDisabled(provider: String) {
            Log.e(TAG, "onProviderDisabled: $provider")
        }

        override fun onProviderEnabled(provider: String) {
            Log.e(TAG, "onProviderEnabled: $provider")
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Log.e(TAG, "onStatusChanged: $provider")
        }

        init {
            Log.e(TAG, "LocationListener $provider")
            mLastLocation = Location(provider)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "SOSAlertTrackingSystem"
        val channel = NotificationChannel(
            CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "SOSAlert Tracking Notification"
            enableLights(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(channel)
    }

}