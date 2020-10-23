package com.emergency.sosalert.discussion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.emergency.sosalert.R

class DiscussionDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discussion_details)
        val disc = intent.getParcelableExtra<Discussion>("discussionDetails")

    }
}