package com.emergency.sosalert.main

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emergency.sosalert.R
import com.emergency.sosalert.chat.ChatList
import com.emergency.sosalert.chat.ChatListAdapter
import com.emergency.sosalert.chat.SearchFriend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_chat.*

@Suppress("UNCHECKED_CAST")
class Chat : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatListRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        applyData()

        friendRefreshBtn.setOnClickListener {
            chatListRecycler.adapter = null
            applyData()
        }

        searchFriend.setOnClickListener {
            startActivity(Intent(context, SearchFriend::class.java))
        }

    }

    private fun applyData() {
        val currentUid = FirebaseAuth.getInstance().currentUser!!.uid
        val currentUserRef = FirebaseFirestore.getInstance().collection("user").document(currentUid)
        currentUserRef.get().addOnSuccessListener {
            if (it.data!!["chatgroup"] != null) {
                val tempList = it.data!!["chatgroup"] as List<String>
                val chatListList = ArrayList<ChatList>()
                for (cg in tempList) {
                    val temp = cg.split(",")
                    lateinit var targetUid: String
                    targetUid = if (temp[0] == currentUid) {
                        temp[1]
                    } else {
                        temp[0]
                    }
                    val tempChatList = ChatList()
                    tempChatList.chatgroupuid = cg
                    tempChatList.target = targetUid
                    chatListList.add(tempChatList)
                }
                noFriendText.visibility = View.GONE
                chatListLoading.visibility = View.GONE
                chatListRecycler.adapter = ChatListAdapter(chatListList)
            } else {
                chatListLoading.visibility = View.GONE
                noFriendText.visibility = View.VISIBLE
            }
        }


    }
}