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
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_main_login.*


class MainLogin : Fragment() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private val TOGGLE_OFF = 0
    private val TOGGLE_ON = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        registerbtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainLogin_to_registerEmail)
        }

        google_sign_in.setOnClickListener {
            progressBarToggle(1)
            allButtonToggle(0)
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, 1)
        }

        loginbtn.setOnClickListener {
            progressBarToggle(1)
            allButtonToggle(0)
            val email = inputEmail.text.toString()
            val password = inputPass.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                invalidLogin()
                progressBarToggle(0)
                allButtonToggle(1)
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            startActivity(Intent(requireContext(), MainActivity::class.java))
                            activity?.finish()
                        } else {
                            invalidLogin()
                            progressBarToggle(0)
                            allButtonToggle(1)
                        }
                    }
            }
        }

        resetpassbtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainLogin_to_resetPassword)
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

    private fun progressBarToggle(mode: Int) {
        if (mode == TOGGLE_ON) {
            progressBar.visibility == View.VISIBLE
        } else if (mode == TOGGLE_OFF) {
            progressBar.visibility == View.GONE
        }
    }

    private fun allButtonToggle(mode: Int) {
        if (mode == TOGGLE_OFF) {
            loginbtn.isEnabled = false
            registerbtn.isEnabled = false
            resetpassbtn.isEnabled = false
            google_sign_in.isEnabled = false

        } else if (mode == TOGGLE_ON) {
            loginbtn.isEnabled = true
            registerbtn.isEnabled = true
            resetpassbtn.isEnabled = true
            google_sign_in.isEnabled = true

        }
    }

    private fun invalidLogin() {
        Snackbar.make(
            requireView(),
            "Invalid login credentials, please try again",
            Snackbar.LENGTH_LONG
        ).show()
    }

}