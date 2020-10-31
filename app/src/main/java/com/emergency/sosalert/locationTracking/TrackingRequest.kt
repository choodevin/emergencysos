package com.emergency.sosalert.locationTracking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.emergency.sosalert.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_tracking_request.*

class TrackingRequest : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking_request)
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
            finish()
        }
    }
}