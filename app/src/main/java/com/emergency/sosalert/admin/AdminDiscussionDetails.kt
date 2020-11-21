package com.emergency.sosalert.admin

import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.marginStart
import com.bumptech.glide.Glide
import com.emergency.sosalert.R
import com.emergency.sosalert.discussion.Discussion
import com.emergency.sosalert.firebaseMessaging.NotificationData
import com.emergency.sosalert.firebaseMessaging.PushNotification
import com.emergency.sosalert.firebaseMessaging.RetrofitInstance
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_admin_discussion_details.*
import kotlinx.android.synthetic.main.activity_discussion_details.description
import kotlinx.android.synthetic.main.activity_discussion_details.discImage
import kotlinx.android.synthetic.main.activity_discussion_details.titleText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdminDiscussionDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_discussion_details)
        val disc = intent.getParcelableExtra<Discussion>("discussiondetails")
        userImage.clipToOutline = true
        if (disc != null) {
            titleText.text = disc.title
            description.text = disc.description
            FirebaseStorage.getInstance()
                .getReferenceFromUrl(disc.imageUrl).downloadUrl.addOnSuccessListener { uri ->
                    if (uri != null) {
                        Glide.with(this).load(uri).into(discImage)
                        image_loading_bar.visibility = View.GONE
                        discImage.setOnClickListener {
                            val i = Intent(Intent.ACTION_VIEW)
                            i.setDataAndType(uri, "image/jpeg")
                            startActivity(i)
                        }
                    }
                }

            backBtn2.setOnClickListener {
                onBackPressed()
            }

            FirebaseFirestore.getInstance().collection("user").document(disc.ownerUid).get()
                .addOnSuccessListener {
                    ownerText.text = it.data!!["name"].toString()
                }

            FirebaseStorage.getInstance().reference.child("profilepicture/${disc.ownerUid}").downloadUrl.addOnSuccessListener {
                Glide.with(this).load(it).into(userImage)
            }

            approveBtn.setOnClickListener {
                FirebaseFirestore.getInstance().collection("discussion").document(disc.commentgroup)
                    .update("status", "approved")
                finish()
            }

            declineBtn.setOnClickListener {
                val dialogBuilder = AlertDialog.Builder(this)
                val reasonText = EditText(this)
                reasonText.isSingleLine = true
                val frameLayout = FrameLayout(this)
                val params = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.marginStart = 15
                params.marginEnd = 15
                reasonText.hint = "enter reason"
                reasonText.layoutParams = params
                frameLayout.addView(reasonText)
                dialogBuilder
                    .setTitle("Decline reason")
                    .setView(frameLayout)
                    .setPositiveButton("Proceed") { _: DialogInterface, _: Int ->
                        if (reasonText.text.isNotEmpty()) {
                            FirebaseFirestore.getInstance().collection("discussion")
                                .document(disc.commentgroup)
                                .update("status", "declined, ${reasonText.text}")
                            FirebaseFirestore.getInstance().collection("user")
                                .document(disc.ownerUid).get().addOnSuccessListener {
                                    PushNotification(
                                        NotificationData(
                                            "One of your discussion has been declined",
                                            "Reason: ${reasonText.text}",
                                            "125", "125",
                                            "no"
                                        ),
                                        it.data!!["token"].toString()
                                    ).also { notif ->
                                        sendNotification(notif)
                                    }
                                }
                            finish()
                        } else {
                            Toast.makeText(this, "Do not leave it empty!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    .setNegativeButton("Cancel") { _: DialogInterface, _: Int ->

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
        } else {
            Toast.makeText(
                this,
                "There was an error retrieving discussion details",
                Toast.LENGTH_LONG
            ).show()
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
