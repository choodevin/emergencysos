package com.emergency.sosalert

import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.emergency.sosalert.locationTracking.LatLong
import com.emergency.sosalert.locationTracking.LocationTrackingService
import com.emergency.sosalert.main.*
import com.emergency.sosalert.main.Map
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var backPressedTime: Long = 0

    private val discussionFragment = Discussion()
    private val sosFragment = Sos()
    private val mapFragment = Map()
    private val profileFragment = UserProfile()
    private val chatFragment = Chat()

    override fun onCreate(savedInstanceState: Bundle?) {

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            FirebaseFirestore.getInstance().collection("user")
                .document(auth.currentUser!!.uid).get().addOnSuccessListener {
                    if (it.exists()) {
                        registerPreferences()
                    } else {
                        FirebaseAuth.getInstance().signOut()

                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build()
                        val googleSignInClient = GoogleSignIn.getClient(this, gso)
                        googleSignInClient.signOut()

                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                }
        }

        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_bot_nav.selectedItemId = R.id.sos_page
        supportFragmentManager.beginTransaction().add(R.id.main_container, sosFragment).commit()

        main_bot_nav.setOnNavigationItemSelectedListener {
            val fragmentManager = supportFragmentManager
            when (it.itemId) {
                R.id.discussion_page -> {
                    hideAllFragment(fragmentManager)
                    if (!discussionFragment.isAdded) {
                        supportFragmentManager.beginTransaction()
                            .add(R.id.main_container, discussionFragment).commit()
                    }
                    fragmentManager.beginTransaction().show(discussionFragment).commit()
                    true
                }
                R.id.sos_page -> {
                    hideAllFragment(fragmentManager)
                    if (!sosFragment.isAdded) {
                        supportFragmentManager.beginTransaction()
                            .add(R.id.main_container, sosFragment).commit()
                    }
                    fragmentManager.beginTransaction().show(sosFragment).commit()
                    true
                }
                R.id.map_page -> {
                    hideAllFragment(fragmentManager)
                    if (!mapFragment.isAdded) {
                        supportFragmentManager.beginTransaction()
                            .add(R.id.main_container, mapFragment).commit()
                    }
                    fragmentManager.beginTransaction().show(mapFragment).commit()
                    true
                }
                R.id.profile_page -> {
                    hideAllFragment(fragmentManager)
                    if (!profileFragment.isAdded) {
                        supportFragmentManager.beginTransaction()
                            .add(R.id.main_container, profileFragment).commit()
                    }
                    fragmentManager.beginTransaction().show(profileFragment).commit()
                    true
                }
                R.id.chat_page -> {
                    hideAllFragment(fragmentManager)
                    if (!chatFragment.isAdded) {
                        supportFragmentManager.beginTransaction()
                            .add(R.id.main_container, chatFragment).commit()
                    }
                    fragmentManager.beginTransaction().show(chatFragment).commit()
                    true
                }
                else -> false
            }
        }
    }

    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
            return;
        } else {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Press back again to exit",
                Snackbar.LENGTH_SHORT
            )
                .show()
        }
        backPressedTime = System.currentTimeMillis();
    }

    private fun hideAllFragment(fm: FragmentManager) {
        fm.beginTransaction().hide(discussionFragment).commit()
        fm.beginTransaction().hide(sosFragment).commit()
        fm.beginTransaction().hide(mapFragment).commit()
        fm.beginTransaction().hide(profileFragment).commit()
        fm.beginTransaction().hide(chatFragment).commit()
    }

    companion object {
        fun updateLocation(location: Location) {
            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            FirebaseDatabase.getInstance().reference.child("userlocation/$uid").setValue(
                LatLong(location.latitude.toString(), location.longitude.toString())
            )
        }
    }

    private fun registerPreferences() {
        val trackPref = PreferenceManager.getDefaultSharedPreferences(this)
        val isTrackingOn = trackPref.getBoolean("enable_tracking", false)
        if (isTrackingOn) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this, LocationTrackingService::class.java))
                Log.e("TRACKER STATUS: ", "tracker on")
            } else {
                startService(Intent(this, LocationTrackingService::class.java))
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(
                    Intent(
                        this,
                        LocationTrackingService::class.java
                    ).putExtra("stop", true)
                )
            } else {
                startService(
                    Intent(this, LocationTrackingService::class.java).putExtra(
                        "stop",
                        true
                    )
                )
            }
        }
    }
}

