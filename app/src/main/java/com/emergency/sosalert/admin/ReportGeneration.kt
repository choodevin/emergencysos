package com.emergency.sosalert.admin

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.emergency.sosalert.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_report_generation.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

@Suppress("DEPRECATION")
class ReportGeneration : Fragment() {
    private val ref = FirebaseFirestore.getInstance()

    //summary
    private var discussioncount = 0
    private var index = 0
    private var pendingcount = 0
    private var approvedcount = 0
    private var declinedcount = 0

    //currentMonth
    private var c_discussioncount = 0
    private var c_pendingcount = 0
    private var c_approvedcount = 0
    private var c_declinedcount = 0

    //previousMonth
    private var p_discussioncount = 0
    private var p_pendingcount = 0
    private var p_approvedcount = 0
    private var p_declinedcount = 0

    private lateinit var tempMonth: Date

    @RequiresApi(Build.VERSION_CODES.O)
    private val currentMonth = LocalDateTime.now().month.toString()

    @RequiresApi(Build.VERSION_CODES.O)
    private val lastMonth = LocalDateTime.now().monthValue

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_report_generation, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //number of user
        ref.collection("user").get().addOnSuccessListener {
            var userCount = 0
            for (document in it) {
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

            for (document in it) {
                tempMonth = it.documents[index].getTimestamp("uploadtime")?.toDate() as Date
                discussioncount += 1
                when {
                    it.documents[index].get("status").toString().compareTo("pending") == 0 -> {
                        pendingcount += 1
                    }
                    it.documents[index].get("status").toString().compareTo("approved") == 0 -> {
                        approvedcount += 1
                    }
                    it.documents[index].get("status").toString().substringBefore(",")
                        .compareTo("declined") == 0 -> {
                        declinedcount += 1
                    }
                }
                if (monthInFullText(tempMonth.month).compareTo(currentMonth) == 0) {
                    c_discussioncount += 1
                    when {
                        it.documents[index].get("status").toString().compareTo("pending") == 0 -> {
                            c_pendingcount += 1
                        }
                        it.documents[index].get("status").toString().compareTo("approved") == 0 -> {
                            c_approvedcount += 1
                        }
                        it.documents[index].get("status").toString().substringBefore(",")
                            .compareTo("declined") == 0 -> {
                            c_declinedcount += 1
                        }
                    }
                }
                if (monthInFullText(tempMonth.month).compareTo(monthInFullText(lastMonth - 2)) == 0) {
                    p_discussioncount += 1
                    when {
                        it.documents[index].get("status").toString().compareTo("pending") == 0 -> {
                            p_pendingcount += 1
                        }
                        it.documents[index].get("status").toString().compareTo("approved") == 0 -> {
                            p_approvedcount += 1
                        }
                        it.documents[index].get("status").toString().substringBefore(",")
                            .compareTo("declined") == 0 -> {
                            p_declinedcount += 1
                        }
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
        btnPreviousMonth.setOnClickListener { viewPrevious() }
        btnCurrentMonth.setOnClickListener { viewCurrent() }
        btnSummary.setOnClickListener { viewSummary() }
    }

    private fun monthInFullText(monthHere: Int): String {
        when (monthHere) {
            0 -> return "JANUARY"
            1 -> return "FEBRUARY"
            2 -> return "MARCH"
            3 -> return "APRIL"
            4 -> return "MAY"
            5 -> return "JUNE"
            6 -> return "JULY"
            7 -> return "AUGUST"
            8 -> return "SEPTEMBER"
            9 -> return "OCTOBER"
            10 -> return "NOVEMBER"
            11 -> return "DECEMBER"
        }
        return "null"
    }

    private fun viewSummary() {
        discussion_count.text = discussioncount.toString()
        //number of discussions pending
        d_pending_count.text = pendingcount.toString()
        //number of discussions declined
        d_declined_count.text = declinedcount.toString()
        //number of discussions approved
        d_approved_count.text = approvedcount.toString()
    }

    private fun viewPrevious() {
        discussion_count.text = p_discussioncount.toString()
        //number of discussions pending
        d_pending_count.text = p_pendingcount.toString()
        //number of discussions declined
        d_declined_count.text = p_declinedcount.toString()
        //number of discussions approved
        d_approved_count.text = p_approvedcount.toString()
    }

    private fun viewCurrent() {
        discussion_count.text = c_discussioncount.toString()
        //number of discussions pending
        d_pending_count.text = c_pendingcount.toString()
        //number of discussions declined
        d_declined_count.text = c_declinedcount.toString()
        //number of discussions approved
        d_approved_count.text = c_approvedcount.toString()
    }
}
