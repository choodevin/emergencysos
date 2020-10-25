package com.emergency.sosalert.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.emergency.sosalert.R
import com.google.firebase.firestore.FirebaseFirestore

class SearchFriend : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_friend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fireStoreRef = FirebaseFirestore.getInstance()
        fireStoreRef.collection("user").whereEqualTo("name","abc")
    }
}