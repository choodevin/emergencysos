package com.emergency.sosalert.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emergency.sosalert.R
import com.emergency.sosalert.login.User
import com.google.firebase.storage.FirebaseStorage

class UserAdapter(
    private val userList: ArrayList<User>
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.userName)
        val image = itemView.findViewById<ImageView>(R.id.userImage)
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
        holder.name.text = userList[position].name
        holder.image.clipToOutline = true
        FirebaseStorage.getInstance().reference.child("profilepicture/${userList[position].uid}").downloadUrl.addOnSuccessListener {
            Glide.with(holder.itemView).load(it).into(holder.image)
        }

    }
}