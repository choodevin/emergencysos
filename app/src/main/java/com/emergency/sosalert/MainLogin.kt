package com.emergency.sosalert

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.fragment_main_login.*


class MainLogin : Fragment() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_login, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                activity?.finish()
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isOnline()) {
            Snackbar.make(view, "You are not connected to the internet", Snackbar.LENGTH_LONG)
                .show()
        }
        registerbtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainLogin_to_registerEmail)
        }

        google_sign_in.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build()
            auth = FirebaseAuth.getInstance()
            googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "Google Authentication: " + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    if (task.result!!.additionalUserInfo!!.isNewUser) {
                        val bundle = Bundle()
                        findNavController().navigate(
                            R.id.action_mainLogin_to_registerPicture,
                            bundle
                        )
                    } else {
                        startActivity(Intent(requireActivity(), MainActivity::class.java))
                        activity?.finish()
                    }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Snackbar.make(requireView(), "Authentication Failed.", Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun isOnline(): Boolean {
        return true
    }

}