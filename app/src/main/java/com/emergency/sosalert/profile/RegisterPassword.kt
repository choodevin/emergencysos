package com.emergency.sosalert.profile

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.emergency.sosalert.R
import kotlinx.android.synthetic.main.fragment_register_password.*
import java.util.regex.Pattern

class RegisterPassword : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showPass.setOnClickListener {
            if (showPass.isChecked) {
                passwordInput.transformationMethod = HideReturnsTransformationMethod.getInstance()
                confirmPasswordInput.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            } else {
                passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
                confirmPasswordInput.transformationMethod =
                    PasswordTransformationMethod.getInstance()
            }
        }

        continueBtn.setOnClickListener {
            val passwordPattern = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,20}$"
            val pass = passwordInput.text.toString()
            val cpass = confirmPasswordInput.text.toString()
            if (pass == cpass) {
                if (Pattern.matches(passwordPattern, pass)) {
                    arguments?.putString("password", passwordInput.text.toString())
                    findNavController().navigate(
                        R.id.action_registerPassword_to_registerName,
                        arguments
                    )
                } else {
                    confirmPasswordInput.error = "Invalid password"
                }
            } else {
                confirmPasswordInput.error = "Password doesn't match"
            }
        }
        backBtn.setOnClickListener {
            activity?.onBackPressed()
        }
    }

}