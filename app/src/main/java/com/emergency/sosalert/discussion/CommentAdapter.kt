package com.emergency.sosalert.discussion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emergency.sosalert.R
import com.emergency.sosalert.login.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class CommentAdapter(
    private val commentList: ArrayList<Comment>
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val owner = itemView.findViewById<TextView>(R.id.comment_poster)
        val details = itemView.findViewById<TextView>(R.id.comment_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.comment_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.details.text = commentList[position].content
        FirebaseFirestore.getInstance().collection("user").document(commentList[position].owner)
            .get().addOnSuccessListener {
                val user = it.data?.get("name").toString()
                holder.owner.text = user
            }
    }
}