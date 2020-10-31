package com.emergency.sosalert.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.emergency.sosalert.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_report_generation.*

class ReportGeneration: Fragment() {
    val ref = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_report_generation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //number of user
        ref.collection("user").get().addOnSuccessListener {
            var userCount = 0
            for(document in it){
                userCount += 1
            }
            user_count.text = userCount.toString()
        }
        //number of button pressed
        ref.collection("report").document("count").get().addOnSuccessListener {
            button_count.text = it.get("buttonpress").toString()
        }
        //number of discussions made
        ref.collection("discussion").get().addOnSuccessListener {
            var discussioncount = 0
            var index = 0
            var pendingcount = 0
            var approvedcount = 0
            var declinedcount = 0
            for (document in it){
                discussioncount += 1
                when {
                    it.documents[index].get("status").toString().compareTo("pending") == 0 -> {
                        pendingcount += 1
                    }
                    it.documents[index].get("status").toString().compareTo("approved") == 0 -> {
                        approvedcount += 1
                    }
                    it.documents[index].get("status").toString().compareTo("declined") == 0 -> {
                        declinedcount += 1
                    }
                }

                index += 1
            }
            discussion_count.text = discussioncount.toString()
            //number of discussions pending
            d_pending_count.text = pendingcount.toString()
            //number of discussions declined
            d_declined_count.text = declinedcount.toString()
            //number of discussions approved
            d_approved_count.text = approvedcount.toString()
        }
        //number of sos successfully received by user
        ref.collection("report").document("count").get().addOnSuccessListener {
            help_count.text = it.get("intentpress").toString()
        }
    }
}