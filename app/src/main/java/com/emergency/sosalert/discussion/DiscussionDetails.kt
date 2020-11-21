package com.emergency.sosalert.discussion

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ablanco.zoomy.Zoomy
import com.bumptech.glide.Glide
import com.emergency.sosalert.R
import com.emergency.sosalert.firebaseMessaging.NotificationData
import com.emergency.sosalert.firebaseMessaging.PushNotification
import com.emergency.sosalert.firebaseMessaging.RetrofitInstance
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_discussion_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class DiscussionDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discussion_details)
        val disc = intent.getParcelableExtra<Discussion>("discussiondetails")
        backBtnDiscDet.setOnClickListener {
            onBackPressed()
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
                    val builder = Zoomy.Builder(this).target(discImage).enableImmersiveMode(false)
                    builder.register()
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

                    val fs = FirebaseFirestore.getInstance()
                    fs.collection("discussion")
                        .whereEqualTo("commentgroup", disc.commentgroup).get()
                        .addOnSuccessListener { ds ->
                            if (ds != null) {
                                for (dis in ds) {
                                    fs.collection("discussion").document(dis.id)
                                        .update("commentcount", FieldValue.increment(1))

                                    if (disc.ownerUid != FirebaseAuth.getInstance().currentUser?.uid) {
                                        FirebaseFirestore.getInstance().collection("user")
                                            .document(c.owner).get()
                                            .addOnSuccessListener { ownerData ->
                                                FirebaseFirestore.getInstance().collection("user")
                                                    .document(disc.ownerUid).get()
                                                    .addOnSuccessListener { discOwner ->
                                                        PushNotification(
                                                            NotificationData(
                                                                "SOSAlert|${dis.id}",
                                                                "${
                                                                    ownerData.get("name").toString()
                                                                } commented on your post",
                                                                "12344",
                                                                "43211",
                                                                ""
                                                            ),
                                                            discOwner.get("token").toString()
                                                        ).also { notif ->
                                                            sendNotification(notif)
                                                        }

                                                    }
                                            }
                                    }
                                    break
                                }
                            }


                        }

                    commentInput.text.clear()
                    noCommentText.visibility = View.GONE
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