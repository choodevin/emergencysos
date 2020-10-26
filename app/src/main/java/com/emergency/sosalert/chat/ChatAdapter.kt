package com.emergency.sosalert.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emergency.sosalert.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.sender_bubble.view.*
import kotlinx.android.synthetic.main.sender_bubble.view.message_text
import kotlinx.android.synthetic.main.target_bubble.view.*

class ChatAdapter(
    private val chatList: ArrayList<Chat>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val SENDER_VIEW = 0
    private val TARGET_VIEW = 1

    class SenderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val message = itemView.message_text
        fun bind(chat: Chat) {
            message.text = chat.message
        }
    }

    class TargetHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val message = itemView.message_text
        fun bind(chat: Chat) {
            message.text = chat.message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context)
        return if (viewType == SENDER_VIEW) {
            SenderHolder(v.inflate(R.layout.sender_bubble, parent, false))
        } else {
            TargetHolder(v.inflate(R.layout.target_bubble, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        val chatSender = chatList[position].sender
        return if (chatSender == FirebaseAuth.getInstance().currentUser!!.uid) {
            SENDER_VIEW
        } else {
            TARGET_VIEW
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(
        viewHolder: RecyclerView.ViewHolder,
        position: Int
    ) {
        if (viewHolder.itemViewType == SENDER_VIEW) {
            SenderHolder(viewHolder.itemView).bind(chatList[position])
        } else {
            TargetHolder(viewHolder.itemView).bind(chatList[position])
        }
    }
}