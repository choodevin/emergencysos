package com.emergency.sosalert.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.emergency.sosalert.R
import com.emergency.sosalert.discussion.Discussion
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_admin_discussion_details.*
import kotlinx.android.synthetic.main.activity_discussion_details.description
import kotlinx.android.synthetic.main.activity_discussion_details.discImage
import kotlinx.android.synthetic.main.activity_discussion_details.titleText

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
                FirebaseFirestore.getInstance().collection("discussion").document(disc.commentgroup)
                    .update("status", "declined")
                finish()
            }
        } else {
            Toast.makeText(
                this,
                "There was an error retrieving discussion details",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
