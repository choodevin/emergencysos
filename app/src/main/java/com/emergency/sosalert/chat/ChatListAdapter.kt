package com.emergency.sosalert.chat


import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.chat_list_item.view.*

class ChatListAdapter(
    private val chatListList: ArrayList<ChatList>
) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var friendname = itemView.friend_name
        var image = itemView.user_image
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =
            LayoutInflater.from(parent.context)
                .inflate(com.emergency.sosalert.R.layout.chat_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return chatListList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.image.clipToOutline = true

        FirebaseFirestore.getInstance().collection("user").document(chatListList[position].target)
            .get()
            .addOnSuccessListener {
                holder.friendname.text = it["name"].toString()
            }

        FirebaseStorage.getInstance().reference.child("profilepicture/${chatListList[position].target}").downloadUrl.addOnSuccessListener {
            Glide.with(holder.itemView.context).load(it).into(holder.image)
        }

        holder.itemView.setOnClickListener {
            val viewChatIntent = Intent(holder.itemView.context, ChatDetails::class.java).putExtra(
                "chatgroupid",
                chatListList[position].chatgroupuid
            )
            holder.itemView.context.startActivity(viewChatIntent)
        }
    }
}