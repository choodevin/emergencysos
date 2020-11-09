package com.emergency.sosalert.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emergency.sosalert.R
import com.emergency.sosalert.login.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_search_friend.*

class SearchFriend : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_friend)
        if (intent.extras?.get("predetermine") != null) {
            val toFind = intent.extras?.get("predetermine").toString()
            FirebaseFirestore.getInstance().collection("user").document(toFind).get()
                .addOnSuccessListener {
                    search_by_email.setText(it.data!!["email"].toString())
                    applySearch.performClick()
                }
        }

        closeButtonSearch.setOnClickListener {
            finish()
        }

        friendRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        applySearch.setOnClickListener {
            val inputEmail = search_by_email.text.toString()
            FirebaseFirestore.getInstance().collection("user").whereEqualTo("email", inputEmail)
                .get().addOnSuccessListener {
                    val userList = ArrayList<User>()
                    for (user in it) {
                        if (user.id != FirebaseAuth.getInstance().currentUser!!.uid) {
                            val tempUser = User()
                            tempUser.uid = user.id
                            tempUser.name = user["name"].toString()
                            userList.add(tempUser)
                        }
                    }
                    friendRecycler.adapter = FriendListAdapter(userList)
                }
        }
    }
}