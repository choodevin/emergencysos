package com.emergency.sosalert.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emergency.sosalert.R
import com.emergency.sosalert.login.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.friend_list_item.view.*

class FriendListAdapter(
    private val userList: ArrayList<User>
) : RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.userName
        val image = itemView.userImage
        val addFriendButton = itemView.addFriendBtn
        val addedFriend = itemView.addedFriend
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.friend_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        val currentUserRef = FirebaseFirestore.getInstance().collection("user").document(
            currentUserUid!!
        )
        val targetUserRef = FirebaseFirestore.getInstance().collection("user").document(
            userList[position].uid
        )

        holder.name.text = userList[position].name
        holder.image.clipToOutline = true
        FirebaseStorage.getInstance().reference.child("profilepicture/${userList[position].uid}").downloadUrl.addOnSuccessListener {
            Glide.with(holder.itemView).load(it).into(holder.image)
        }

        currentUserRef.get().addOnSuccessListener {
            if (it["chatgroup"] != null) {
                val tempList = it["chatgroup"] as List<String>
                for (cg in tempList) {
                    val temp = cg.split(",")
                    if (currentUserUid in temp && userList[position].uid in temp) {
                        holder.addFriendButton.visibility = View.GONE
                        holder.addedFriend.visibility = View.VISIBLE
                    }
                }
            }
        }

        holder.addFriendButton.setOnClickListener {
            val chatGroupid =
                "${currentUserUid},${userList[position].uid}"

            currentUserRef.update("chatgroup", FieldValue.arrayUnion(chatGroupid))
            targetUserRef.update("chatgroup", FieldValue.arrayUnion(chatGroupid))

            holder.addFriendButton.visibility = View.GONE
            holder.addedFriend.visibility = View.VISIBLE
        }
    }
}