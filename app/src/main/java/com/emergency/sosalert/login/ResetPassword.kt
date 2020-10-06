package com.emergency.sosalert.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.emergency.sosalert.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_register_email.*
import kotlinx.android.synthetic.main.fragment_register_gender.*
import kotlinx.android.synthetic.main.fragment_register_gender.backBtn
import kotlinx.android.synthetic.main.fragment_register_gender.continueBtn
import java.util.regex.Pattern

class ResetPassword : Fragment() {
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reset_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        backBtn.setOnClickListener {
            activity?.onBackPressed()
        }

        continueBtn.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            backBtn.visibility = View.GONE
            auth = FirebaseAuth.getInstance()
            val email = inputEmail.text.toString()
            val emailRegex = Pattern.compile(
                "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                        "\\@" +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                        "(" +
                        "\\." +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                        ")+"
            )
            if (!emailRegex.matcher(email).matches()) {
                Snackbar.make(requireView(), "Enter email in correct format", Snackbar.LENGTH_LONG)
                    .show()
                progressBar.visibility = View.GONE
                backBtn.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (email.isBlank()) {
                Snackbar.make(requireView(), "Do not leave email empty", Snackbar.LENGTH_LONG)
                    .show()
                progressBar.visibility = View.GONE
                backBtn.visibility = View.VISIBLE
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email).addOnCompleteListener {
                if (it.isSuccessful) {
                    Snackbar.make(
                        requireView(),
                        "Reset email has been sent, check your email",
                        Snackbar.LENGTH_LONG
                    ).show()
                } else {
                    Snackbar.make(
                        requireView(),
                        "Email is not registered, please try another email",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                progressBar.visibility = View.GONE
                backBtn.visibility = View.VISIBLE
            }
        }
    }
}