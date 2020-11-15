package com.emergency.sosalert

import android.content.Intent
import android.content.SharedPreferences
import android.net.sip.SipSession
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.emergency.sosalert.locationTracking.LocationTrackingService
import com.emergency.sosalert.locationTracking.ModifyPermission
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_user_profile.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val trackingPref = findPreference<SwitchPreference>("enable_tracking")
            val modPerm = findPreference<Preference>("modify_tracking_permission")
            val logout = findPreference<Preference>("logout")
            trackingPref?.setOnPreferenceClickListener {
                Toast.makeText(
                    context,
                    "Please restart the application to make the changes",
                    Toast.LENGTH_LONG
                ).show()

                if (trackingPref.isChecked) {
                    FirebaseFirestore.getInstance().collection("user")
                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
                        .update("allowTracking", true)
                } else {
                    FirebaseFirestore.getInstance().collection("user")
                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
                        .update("allowTracking", false)
                }
                return@setOnPreferenceClickListener false
            }

            modPerm?.setOnPreferenceClickListener {
                startActivity(
                    Intent(
                        context,
                        ModifyPermission::class.java
                    )
                )
                return@setOnPreferenceClickListener false
            }

            logout?.setOnPreferenceClickListener {
                FirebaseFirestore.getInstance().collection("user")
                    .document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .update("token", "empty")
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(context, LoginActivity::class.java))
                return@setOnPreferenceClickListener false
            }


        }
    }
}