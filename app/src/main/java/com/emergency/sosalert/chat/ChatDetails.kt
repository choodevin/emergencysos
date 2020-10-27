package com.emergency.sosalert.chat

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.emergency.sosalert.R
import com.emergency.sosalert.firebaseMessaging.NotificationData
import com.emergency.sosalert.firebaseMessaging.PushNotification
import com.emergency.sosalert.firebaseMessaging.RetrofitInstance
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


class ChatDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_details)
        targetUserImage.clipToOutline = true
        val chatgroupid = intent.extras?.get("chatgroupid") as String
        val temp = chatgroupid.split(",")
        val currentUid = FirebaseAuth.getInstance().currentUser!!.uid
        var targetUid: String
        targetUid = if (temp[0] == currentUid) {
            temp[1]
        } else {
            temp[0]
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

            closeKeyboard()
        }
    }

    private fun getMessages(chatgroupid: String) {
        FirebaseDatabase.getInstance().reference.child("chatgroup/$chatgroupid")
            .orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var messageList = ArrayList<Chat>()
                    snapshot.children.forEach { it ->
                        val tempChat = it.getValue(Chat::class.java)
                        messageList.add(tempChat!!)
                    }
                    messagesRecycler.adapter = ChatAdapter(messageList)
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
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