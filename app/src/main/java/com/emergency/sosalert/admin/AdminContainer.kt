package com.emergency.sosalert.admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.emergency.sosalert.R
import kotlinx.android.synthetic.main.activity_main.*

class AdminContainer : AppCompatActivity() {
    private val discussionFragment = AllDiscussion()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        supportFragmentManager.beginTransaction().add(R.id.admin_container, AllDiscussion())
            .commit()

        main_bot_nav.setOnNavigationItemSelectedListener {
            val fragmentManager = supportFragmentManager
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
                R.id.what_you_want -> {
                    TODO()
                }

                else -> false
            }
        }
    }

    private fun hideAllFragment(fm: FragmentManager) {
        fm.beginTransaction().hide(discussionFragment).commit()
    }
}