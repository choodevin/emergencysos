package com.emergency.sosalert.locationTracking

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.emergency.sosalert.R
import com.emergency.sosalert.firebaseMessaging.NotificationData
import com.emergency.sosalert.firebaseMessaging.PushNotification
import com.emergency.sosalert.firebaseMessaging.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_tracking_request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrackingRequest : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking_request)
        trackRequestClose.setOnClickListener {
            finish()
        }
        val toallowid = intent.getStringExtra("toallowid")
        val currentUid = FirebaseAuth.getInstance().currentUser!!.uid
        toallowProfile.clipToOutline = true
        FirebaseStorage.getInstance().reference.child("profilepicture/$toallowid").downloadUrl.addOnSuccessListener {
            Glide.with(this).load(it).into(toallowProfile)
        }

        FirebaseFirestore.getInstance().collection("user").document(toallowid!!).get()
            .addOnSuccessListener {
                toallowName.text = it.data!!["name"].toString()
            }

        allowButton.setOnClickListener {
            FirebaseFirestore.getInstance().collection("user").document(currentUid)
                .update("allowTrackingList", FieldValue.arrayUnion(toallowid))
            finish()
        }

        declineButton.setOnClickListener {
            FirebaseFirestore.getInstance().collection("user").document(toallowid).get()
                .addOnSuccessListener {
                    FirebaseFirestore.getInstance().collection("user")
                        .document(FirebaseAuth.getInstance().currentUser!!.uid).get()
                        .addOnSuccessListener { own ->
                            PushNotification(
                                NotificationData(
                                    "Tracking Request",
                                    "Your tracking request has been rejected by ${own.data!!["name"].toString()}",
                                    "5",
                                    "1",
                                    ""
                                ), it.data!!["token"].toString()
                            ).also { notif ->
                                sendNotification(notif)
                            }
                        }
                }
            finish()
        }
    }

    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postNotification(notification)
                if (response.isSuccessful) {
                    Log.d(ContentValues.TAG, "Response: ${Gson().toJson(response)}")
                } else {
                    Log.e(ContentValues.TAG, response.errorBody().toString())
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, e.toString())
            }
        }
}