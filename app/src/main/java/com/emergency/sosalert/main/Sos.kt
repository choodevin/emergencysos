package com.emergency.sosalert.main

import android.annotation.SuppressLint
import android.app.Notification
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.emergency.sosalert.R
import com.emergency.sosalert.discussion.Discussion
import com.emergency.sosalert.firebaseMessaging.FirebaseService
import com.emergency.sosalert.firebaseMessaging.NotificationData
import com.emergency.sosalert.firebaseMessaging.PushNotification
import com.emergency.sosalert.firebaseMessaging.RetrofitInstance
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.Constants.MessagePayloadKeys.SENDER_ID
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_main_login.*
import kotlinx.android.synthetic.main.fragment_sos.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject


class Sos : Fragment() {

    private var victim = ""
    private var latitude = ""
    private var longitude = ""
    private val uid = FirebaseAuth.getInstance().uid ?: ""
    val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val picref = FirebaseStorage.getInstance().reference.child("profilepicture").child(uid)
        var yeet = ""
        FirebaseService.sharedPref =
            context?.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            FirebaseService.token = it.token
        }
        FirebaseFirestore.getInstance().collection("user").document(uid)
            .update("token", FirebaseService.token)
        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        getLastLocation()
        picref.downloadUrl.addOnSuccessListener {
            val uri = it
            yeet = uri.toString()
            if (sosButton != null) {
                sosButton.isEnabled = true
            }
        }
            sosButton.setOnClickListener {
                val lm: LocationManager =
                    context?.getSystemService(LOCATION_SERVICE) as LocationManager
                var gpsOn = false
                var networkOn = false
                try {
                    gpsOn = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                } catch (ex: Exception) {
                }
                try {
                    networkOn = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                } catch (ex: Exception) {
                }

                if (!gpsOn) {
                    Toast.makeText(requireContext(), "Turn on GPS", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                if (!networkOn) {
                    Toast.makeText(requireContext(), "Turn on network", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val ref = FirebaseFirestore.getInstance()


                ref.collection("user").document(uid).get().addOnSuccessListener { him ->
                    victim = him.data?.get("name").toString()
                    var tempLatitude: Double
                    var tempLongitude: Double
                    var userlocation = Location("")
                    userlocation.latitude = latitude.toDouble()
                    userlocation.longitude = longitude.toDouble()
                    var targetlocation = Location("")
                    try {
                        //pull all user, check geo, pull token send
                        ref.collection("user").get().addOnSuccessListener { main ->
                            var j = 0
                            var maxRange = 100
                            while(j < 1){
                                var i = 0
                            for (document in main) {
                                if (main.documents[i]["token"].toString()
                                        .compareTo(FirebaseService.token.toString()) != 0
                                ) {
                                    tempLatitude =
                                        main.documents[i]["latitude"].toString().toDouble()
                                    tempLongitude =
                                        main.documents[i]["longitude"].toString().toDouble()
                                    var targetName: String = main.documents[i]["name"].toString()
                                    val tokenyeet = main.documents[i]["token"].toString()
                                    targetlocation.latitude = tempLatitude
                                    targetlocation.longitude = tempLongitude
                                    i++
                                    if (userlocation.distanceTo(targetlocation) <= maxRange && userlocation.distanceTo(
                                            targetlocation
                                        ) > 0) {
                                        var distanceboi = userlocation.distanceTo(targetlocation)
                                        PushNotification(
                                            NotificationData(
                                                "Someone is in danger!",
                                                "$targetName ,$victim is in danger!, $distanceboi",
                                                "$latitude",
                                                "$longitude",
                                                "$yeet"
                                            ),
                                            tokenyeet
                                        ).also {
                                            sendNotification(it)
                                            j += 1
                                        }
                                    }
                                }
                            }
                                if(j < 1){
                                    maxRange += 1000
                                }
                        }
                        }
                        Log.e(TAG, "try")
                        FirebaseFirestore.getInstance().collection("report").document("count").get().addOnSuccessListener {
                            var count = it.get("buttonpress").toString().toInt()
                            count += 1
                            FirebaseFirestore.getInstance().collection("report").document("count").update("buttonpress",count)
                        }
                    } catch (e: java.lang.Exception) {
                        Log.e("TAG", "onCreate: " + e.message)
                        Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_LONG).show()
                    }
                }
                sosButton.isEnabled = false
                object : CountDownTimer(11000, 1000) {
                    override fun onFinish() {
                        if (sosButton != null) {
                            sosButton.isEnabled = true
                            timer_title.visibility = View.INVISIBLE
                            countdown_timer.visibility = View.INVISIBLE
                        }
                    }

                    override fun onTick(p0: Long) {
                        if (timer_title != null) {
                            timer_title.visibility = View.VISIBLE
                            countdown_timer.visibility = View.VISIBLE
                            countdown_timer.text = "${p0 / 1000}"
                        }
                    }

                }.start()
            }
        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
        ) {
            if (requestCode == PERMISSION_ID) {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getLastLocation()
                }
            }
        }

        private fun requestPermissions() {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),
                PERMISSION_ID
            )
        }

        private fun checkPermissions(): Boolean {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return true
            }
            return false
        }

        private fun isLocationEnabled(): Boolean {
            var locationManager: LocationManager =
                this.context?.getSystemService(LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        }

        private val mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                var mLastLocation: Location = locationResult.lastLocation
                latitude = mLastLocation.latitude.toString()
                longitude = mLastLocation.longitude.toString()
            }
        }

        @SuppressLint("MissingPermission")
        private fun requestNewLocationData() {
            var mLocationRequest = LocationRequest()
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            mLocationRequest.interval = 5000
            mLocationRequest.fastestInterval = 2500
            mLocationRequest.numUpdates = 1

            mFusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
            mFusedLocationClient!!.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
            )
            FirebaseFirestore.getInstance().collection(
                "user"
            ).document(uid).update("latitude", latitude, "longitude", longitude)
        }

        @SuppressLint("MissingPermission")
        private fun getLastLocation() {
            if (checkPermissions()) {
                if (isLocationEnabled()) {
                    mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                        var location: Location? = task.result
                        if (location == null) {
                            requestNewLocationData()
                        } else {
                            latitude = location.latitude.toString()
                            longitude = location.longitude.toString()
                            FirebaseFirestore.getInstance().collection(
                                "user"
                            ).document(uid).update("latitude", latitude, "longitude", longitude)
                        }
                    }
                } else {
                    Toast.makeText(requireActivity(), "Turn on location", Toast.LENGTH_LONG).show()
                }
            } else {
                requestPermissions()
            }
        }

        private fun sendNotification(notification: PushNotification) =
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitInstance.api.postNotification(notification)
                    if (response.isSuccessful) {
                        Log.d(TAG, "Response: ${Gson().toJson(response)}")
                    } else {
                        Log.e(TAG, response.errorBody().toString())
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                }
            }
    }
