package com.emergency.sosalert.chat

import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.emergency.sosalert.R
import com.emergency.sosalert.firebaseMessaging.NotificationData
import com.emergency.sosalert.firebaseMessaging.PushNotification
import com.emergency.sosalert.firebaseMessaging.RetrofitInstance
import com.emergency.sosalert.locationTracking.TrackerMap
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_chat_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException


@Suppress("UNCHECKED_CAST")
class ChatDetails : AppCompatActivity() {
    private var currentUid: String = ""
    private var targetUid: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_details)
        targetUserImage.clipToOutline = true
        val chatgroupid = intent.extras?.get("chatgroupid") as String
        val temp = chatgroupid.split(",")
        currentUid = FirebaseAuth.getInstance().currentUser!!.uid
        targetUid = if (temp[0] == currentUid) {
            temp[1]
        } else {
            temp[0]
        }

        backBtn.setOnClickListener {
            onBackPressed()
        }

        trackingButton.setOnClickListener {
            FirebaseFirestore.getInstance().collection("user").document(targetUid).get()
                .addOnSuccessListener {
                    if (it != null) {
                        if (it.data?.get("allowTracking") as Boolean) {
                            if (it.data?.get("allowTrackingList") != null) {
                                val allowTrackingList =
                                    it.data?.get("allowTrackingList") as List<String>
                                if (allowTrackingList.contains(currentUid)) {
                                    startActivity(
                                        Intent(this, TrackerMap::class.java).putExtra(
                                            "targetuid",
                                            targetUid
                                        )
                                    )
                                } else {
                                    requestTrackingPermission()
                                }
                            } else {
                                requestTrackingPermission()
                            }
                        } else {
                            val dialogBuilder = AlertDialog.Builder(this)
                            dialogBuilder
                                .setTitle("Unable to track")
                                .setMessage("Your tracking target seems does not have tracking enabled, please tell him/her to enable it and try again.")
                                .setPositiveButton("Ok") { _: DialogInterface, _: Int ->
                                }
                            val dialog = dialogBuilder.create()
                            dialog.setOnShowListener {
                                dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                                    .setTextColor(
                                        ContextCompat.getColor(
                                            applicationContext,
                                            R.color.colorPrimaryDark
                                        )
                                    )
                            }
                            dialog.show()
                        }
                    }
                }
        }

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = false
        layoutManager.stackFromEnd = true
        messagesRecycler.layoutManager = layoutManager

        getMessages(chatgroupid)

        FirebaseFirestore.getInstance().collection("user").document(targetUid).get()
            .addOnSuccessListener {
                targetUser.text = it["name"] as String
            }

        FirebaseStorage.getInstance().reference.child("profilepicture/$targetUid").downloadUrl.addOnSuccessListener {
            Glide.with(this).load(it).into(targetUserImage)
        }

        sendButton.setOnClickListener {
            val messageContent = inputMessage.text.toString()
            val chat = Chat()
            if (messageContent.isEmpty()) {
                return@setOnClickListener
            }

            chat.message = messageContent
            chat.sender = currentUid

            FirebaseDatabase.getInstance().reference.child("chatgroup/$chatgroupid").push()
                .setValue(
                    hashMapOf(
                        "message" to chat.message,
                        "sender" to chat.sender,
                        "timestamp" to ServerValue.TIMESTAMP
                    )
                )


            try {
                FirebaseFirestore.getInstance().collection("user").document(targetUid).get()
                    .addOnSuccessListener { targetData ->
                        FirebaseFirestore.getInstance().collection("user").document(currentUid)
                            .get().addOnSuccessListener { senderData ->
                                FirebaseStorage.getInstance().reference.child("profilepicture/${chat.sender}").downloadUrl.addOnSuccessListener {
                                    PushNotification(
                                        NotificationData(
                                            "${senderData.data!!["name"].toString()}|$chatgroupid",
                                            chat.message,
                                            "999", "0",
                                            it.toString()
                                        ),
                                        targetData.data!!["token"].toString()
                                    ).also { notif ->
                                        sendNotification(notif)
                                    }
                                }
                            }
                    }
            } catch (e: JSONException) {

            }
            inputMessage.text.clear()
        }
    }

    private fun getMessages(chatgroupid: String) {
        FirebaseDatabase.getInstance().reference.child("chatgroup/$chatgroupid")
            .orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messageList = ArrayList<Chat>()
                    snapshot.children.forEach { it ->
                        val tempChat = it.getValue(Chat::class.java)
                        messageList.add(tempChat!!)
                    }
                    chatLoading.visibility = View.GONE
                    messagesRecycler.adapter = ChatAdapter(messageList)
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
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

    private fun requestTrackingPermission() {
        Snackbar.make(
            findViewById(R.id.chat_details),
            "Tracking permission requested.",
            Snackbar.LENGTH_LONG
        ).show()

        FirebaseFirestore.getInstance().collection("user").document(targetUid).get()
            .addOnSuccessListener { targetData ->
                FirebaseFirestore.getInstance().collection("user").document(currentUid)
                    .get().addOnSuccessListener { senderData ->
                        FirebaseStorage.getInstance().reference.child("profilepicture/$currentUid").downloadUrl.addOnSuccessListener {
                            PushNotification(
                                NotificationData(
                                    "Location tracking request|${currentUid}",
                                    "${senderData.data!!["name"].toString()} wants to track your location!",
                                    "888", "0",
                                    it.toString()
                                ),
                                targetData.data!!["token"].toString()
                            ).also { notif ->
                                sendNotification(notif)
                            }
                        }
                    }
            }
    }
}