package com.emergency.sosalert.discussion

import android.content.Context
import android.os.Bundle
import android.renderscript.Sampler
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.emergency.sosalert.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_discussion_details.*

class DiscussionDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discussion_details)
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
            commentRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

            viewCommentBtn.setOnClickListener {
                viewCommentBtn.visibility = View.GONE
                commentRecycler.visibility = View.VISIBLE
                commentInput.visibility = View.VISIBLE
                sendCommentBtn.visibility = View.VISIBLE

                val realtimeRef = FirebaseDatabase.getInstance().reference.child("comments")
                    .child(disc.commentgroup)
                realtimeRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val commentList = ArrayList<Comment>()
                        snapshot.children.forEach {
                            val tempComment = it.getValue(Comment::class.java)
                            commentList.add(tempComment!!)
                        }
                        commentRecycler.adapter = CommentAdapter(commentList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
            }

            sendCommentBtn.setOnClickListener {
                if (commentInput.text.isNullOrEmpty()) {
                    Snackbar.make(it, "You did not enter any comment!", Snackbar.LENGTH_LONG).show()
                } else {
                    val realtimeRef = FirebaseDatabase.getInstance().reference.child("comments")
                        .child(disc.commentgroup)
                    val c = Comment()

                    c.content = commentInput.text.toString()
                    c.owner = FirebaseAuth.getInstance().currentUser!!.uid

                    realtimeRef.push().setValue(c)

                    commentInput.text.clear()
                    closeKeyboard()
                }
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