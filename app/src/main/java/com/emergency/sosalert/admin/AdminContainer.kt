package com.emergency.sosalert.admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.emergency.sosalert.R
import kotlinx.android.synthetic.main.activity_admin.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.main_bot_nav
import kotlinx.android.synthetic.main.activity_modify_permission.*
import java.util.function.ToDoubleBiFunction

class AdminContainer : AppCompatActivity() {
    private val discussionFragment = AllDiscussion()
    private val reportFragment = ReportGeneration()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().add(R.id.admin_container, discussionFragment)
            .commit()

        backBtn3.setOnClickListener {
            onBackPressed()
        }

        main_bot_nav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.all_discussion_page -> {
                    hideAllFragment(fragmentManager)
                    if (!discussionFragment.isAdded) {
                        supportFragmentManager.beginTransaction()
                            .add(R.id.admin_container, discussionFragment).commit()
                    }
                    fragmentManager.beginTransaction().show(discussionFragment).commit()
                    true
                }
                R.id.report_gen_page -> {
                    hideAllFragment(fragmentManager)
                    if (!reportFragment.isAdded) {
                        supportFragmentManager.beginTransaction()
                            .add(R.id.admin_container, reportFragment).commit()
                    }
                    fragmentManager.beginTransaction().show(reportFragment).commit()
                    true
                }
                else -> false
            }
        }
    }

    private fun hideAllFragment(fm: FragmentManager) {
        fm.beginTransaction().hide(discussionFragment).commit()
        fm.beginTransaction().hide(reportFragment).commit()
    }
}