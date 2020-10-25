package com.emergency.sosalert.main

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.emergency.sosalert.R
import com.emergency.sosalert.chat.SearchFriend
import kotlinx.android.synthetic.main.fragment_chat.*

class Chat : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchFriend.setOnClickListener {
            startActivity(Intent(context, SearchFriend::class.java))
        }
    }
}