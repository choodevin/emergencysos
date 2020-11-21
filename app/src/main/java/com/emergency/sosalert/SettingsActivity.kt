package com.emergency.sosalert

import android.content.Intent
import android.content.SharedPreferences
import android.net.sip.SipSession
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.emergency.sosalert.locationTracking.LocationTrackingService
import com.emergency.sosalert.locationTracking.ModifyPermission
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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

    override fun onBackPressed() {
        onSupportNavigateUp()
    }

    override fun onSupportNavigateUp(): Boolean {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        return super.onSupportNavigateUp()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val trackingPref = findPreference<SwitchPreference>("enable_tracking")
            val modPerm = findPreference<Preference>("modify_tracking_permission")
            val logout = findPreference<Preference>("logout")
            trackingPref?.setOnPreferenceClickListener {
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

                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
                val auth = FirebaseAuth.getInstance()

                auth.signOut()
                googleSignInClient.signOut()

                startActivity(Intent(context, LoginActivity::class.java))
                activity?.finish()

                return@setOnPreferenceClickListener false
            }
        }
    }
}