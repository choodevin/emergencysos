package com.emergency.sosalert.discussion

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emergency.sosalert.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_my_post_discussion_details.*

class MyPostDiscussionDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_post_discussion_details)
        val disc = intent.getParcelableExtra<Discussion>("discussiondetails")

        backBtnDiscDet.setOnClickListener {
            onBackPressed()
        }

        deleteDisBtn.setOnClickListener {
            if (disc?.id != null) {
                val fs = FirebaseFirestore.getInstance()
                fs.collection("discussion")
                    .whereEqualTo("commentgroup", disc.commentgroup).get().addOnSuccessListener {
                        for (doc in it) {
                            if (doc != null) {
                                fs.collection("discussion").document(doc.id).delete()
                                FirebaseStorage.getInstance().reference.child("discussionPicture/${doc.id}")
                                    .delete()
                            }
                        }
                    }
                FirebaseDatabase.getInstance().reference.child("comments/${disc.commentgroup}")
                    .removeValue()
                Toast.makeText(this, "Discussion has been removed.", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        if (disc != null) {
            posterImage.clipToOutline = true
            titleText.text = disc.title
            description.text = disc.description
            val firebaseStorage =
                FirebaseStorage.getInstance()
            firebaseStorage.getReferenceFromUrl(disc.imageUrl).downloadUrl.addOnSuccessListener {
                if (it != null) {
                    Glide.with(this).load(it).into(discImage)
                    imageLoading.visibility = View.GONE
                }
            }
            firebaseStorage.reference.child("profilepicture/${disc.ownerUid}").downloadUrl.addOnSuccessListener {
                if (it != null) {
                    Glide.with(this).load(it).into(posterImage)
                }
            }
            FirebaseFirestore.getInstance().collection("user").document(disc.ownerUid).get()
                .addOnSuccessListener {
                    if (it != null) {
                        posterName.text = it.data!!["name"].toString()
                    }
                }
            commentRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

            val realtimeRef = FirebaseDatabase.getInstance().reference.child("comments")
                .child(disc.commentgroup)
            realtimeRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val commentList = ArrayList<Comment>()
                    snapshot.children.forEach {
                        val tempComment = it.getValue(Comment::class.java)
                        commentList.add(tempComment!!)
                    }
                    if (commentList.isEmpty()) {
                        noCommentText.visibility = View.VISIBLE
                    }
                    commentRecycler.adapter = CommentAdapter(commentList)
                    commentLoading.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

            sendCommentBtn.setOnClickListener {
                if (commentInput.text.isNullOrEmpty()) {
                    Snackbar.make(it, "You did not enter any comment!", Snackbar.LENGTH_LONG).show()
                } else {
                    val c = Comment()

                    c.content = commentInput.text.toString()
                    c.owner = FirebaseAuth.getInstance().currentUser!!.uid

                    FirebaseDatabase.getInstance().reference.child("comments")
                        .child(disc.commentgroup).push().setValue(c)

                    commentInput.text.clear()
                    closeKeyboard()
                }
            }

            toMapBtn.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("geo:${disc.latitude},${disc.longitude}?q=${disc.latitude},${disc.longitude}")
                    )
                )
            }
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