package com.emergency.sosalert.admin

import android.content.Context
import android.opengl.Visibility
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emergency.sosalert.R
import com.emergency.sosalert.discussion.Comment
import com.emergency.sosalert.discussion.CommentAdapter
import com.emergency.sosalert.discussion.Discussion
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_admin_discussion_details.*
import kotlinx.android.synthetic.main.activity_discussion_details.*
import kotlinx.android.synthetic.main.activity_discussion_details.description
import kotlinx.android.synthetic.main.activity_discussion_details.discImage
import kotlinx.android.synthetic.main.activity_discussion_details.titleText
import kotlinx.android.synthetic.main.activity_discussion_details.viewCommentBtn

class AdminDiscussionDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_discussion_details)
        val disc = intent.getParcelableExtra<Discussion>("discussiondetails")
        if (disc != null) {
            titleText.text = disc.title
            description.text = disc.description
            FirebaseStorage.getInstance()
                .getReferenceFromUrl(disc.imageUrl).downloadUrl.addOnSuccessListener {
                    if (it != null) {
                        Glide.with(this).load(it).into(discImage)
                    }
                }

            viewCommentBtn.visibility = View.GONE
            approveBtn.setOnClickListener{
                FirebaseFirestore.getInstance().collection("discussion").document(disc.commentgroup).update("status","approved")
                finish()
            }
            declineBtn.setOnClickListener{
                FirebaseFirestore.getInstance().collection("discussion").document(disc.commentgroup).update("status","declined")
                finish()
            }
            //maybe put dialog box
        } else {
            Toast.makeText(
                this,
                "There was an error retrieving discussion details",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
