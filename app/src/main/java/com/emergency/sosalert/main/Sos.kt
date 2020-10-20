package com.emergency.sosalert.main

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_sos.*
import org.json.JSONException
import org.json.JSONObject
import com.emergency.sosalert.R
import com.emergency.sosalert.login.User
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.okhttp.internal.DiskLruCache
import org.w3c.dom.Document
import kotlin.collections.Map

class Sos : Fragment() {
    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey =
        "key=" + "AAAAQfr8GZw:APA91bGNjK9wn8iSg1TAQq2RpNMYac6OwTCDXrpd64jGvB2rn8JZLXcazoXggVpTh9uyBW0OMEeIuldFAqq3JhLILFvFWxmnEumEwzj39UABRR3sn_YjVGzkKQrnp4EX3gRU_ztktoWZ"
    private val contentType = "application/json"
    private var victim = ""
    private var latitude = ""
    private var longitude = ""
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
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val picref = FirebaseStorage.getInstance().reference.child("profilepicture").child(uid)
        var yeet = ""

        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        getLastLocation()

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
            val notification = JSONObject()
            val notificationBody = JSONObject()
            val ref = FirebaseFirestore.getInstance()
            val topic = "/topics/$uid"

            ref.collection("user").document(uid).get().addOnSuccessListener {
                victim = it.data?.get("name").toString()

                try {
                    notificationBody.put("title", "Someone need your help!")
                    notificationBody.put("message", "$victim is in danger, help him/her")
                    //notificationBody.put("latitude", latitude)
                    //notificationBody.put("longitude", longitude)
                    //notificationBody.put("image", yeet)
                    notification.put("to", topic)
                    notification.put("data", notificationBody)
                    sendNotification(notification)
                    Log.e(TAG, "try")
                } catch (e: JSONException) {
                    Log.e("TAG", "onCreate: " + e.message)
                }

            }

            /*val postListener = object :  {
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("errordb", "loadPost:onCancelled", databaseError.toException())
                }

                override fun onDataChange(documentSnapshot: DocumentSnapshot) {
                    val post = documentSnapshot.data
                    victim = post?.get("name").toString()

                    try {
                        notificationBody.put("title", "Someone need your help!")
                        notificationBody.put("message", "$victim is in danger, help him/her")
                        //notificationBody.put("latitude", latitude)
                        //notificationBody.put("longitude", longitude)
                        // notificationBody.put("image", yeet)
                        notification.put("to", uid)
                        notification.put("data", notificationBody)
                        Log.e(TAG, "try")
                    } catch (e: JSONException) {
                        Log.e("TAG", "onCreate: " + e.message)
                    }
                    sendNotification(notification)
                }
            //}
            //ref.addSnapshotListener(postListener)*/
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

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
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
                    }
                }
            } else {
                Toast.makeText(requireActivity(), "Turn on location", Toast.LENGTH_LONG).show()
            }
        } else {
            requestPermissions()
        }
    }
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(this.requireContext())
    }
    private fun sendNotification(notification: JSONObject) {
        Log.e("TAG", "sendNotification")
        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
            Response.Listener<JSONObject> { response ->
                Log.i("TAG", "onResponse: $response")
            },
            Response.ErrorListener {
                Toast.makeText(requireActivity(), it.toString() , Toast.LENGTH_LONG).show()
                Log.i("TAG", "onErrorResponse: Didn't work")
            }) {

            override fun getHeaders(): Map<String,String> {
                val params = HashMap<String, String>()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }
        requestQueue.add(jsonObjectRequest)
    }
}
