package com.emergency.sosalert.main

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emergency.sosalert.R
import com.emergency.sosalert.discussion.CreateDiscussion
import com.emergency.sosalert.discussion.Discussion
import com.emergency.sosalert.discussion.DiscussionRecyclerViewAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_discussion.*
import kotlinx.android.synthetic.main.fragment_register_picture.*

class Discussion : Fragment() {

    private val discussionList = ArrayList<Discussion>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_discussion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addDiscussion.setOnClickListener {
            startActivity(Intent(context, CreateDiscussion::class.java))
        }

        applyData()

        discussionRecycler.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        discussionRefresh.setOnRefreshListener {
            applyData()
        }
    }

    private fun applyData() {
        val discussionRef = FirebaseFirestore.getInstance()
        val discussionStorage = FirebaseStorage.getInstance()
        discussionRef.collection("discussion").get().addOnSuccessListener {
            discussionList.clear()
            for (discussion in it) {
                val disc = discussion.toObject(Discussion::class.java)
                discussionList.add(disc)
            }

            discussionRecycler.adapter = DiscussionRecyclerViewAdapter(discussionList)
            discussionRefresh.isRefreshing = false
        }
    }
}