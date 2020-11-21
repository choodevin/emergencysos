package com.emergency.sosalert.firebaseMessaging

import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SOSRejectReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        val data = p1?.extras!!.get("reject").toString()
        val senderUID = p1.extras!!.get("senderUid").toString()
        val ref = FirebaseFirestore.getInstance()
        var senderToken: String

        ref.collection("user").document(senderUID).get().addOnSuccessListener { it ->
            senderToken = it.data?.get("token").toString()
            PushNotification(
                NotificationData(
                    "SOS rejected",
                    "Your SOS request has been rejected!",
                    "5000.0",
                    "0.0",
                    ""
                ),
                senderToken
            ).also { notify ->
                sendNotification(notify)
            }
        }

        p0.apply {
            this?.let {
                NotificationManagerCompat.from(it).cancel(p1.extras!!.getInt("notificationid"))
            }
        }

        Log.e(TAG, "reject received $data")
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