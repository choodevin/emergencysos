package com.emergency.sosalert.login

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.emergency.sosalert.R
import kotlinx.android.synthetic.main.fragment_register_email.*

class RegisterEmail : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        continueBtn.setOnClickListener {
            if (Patterns.EMAIL_ADDRESS.matcher(search_by_email.text).matches()) {
                val bundle = Bundle()
                bundle.putString("email", search_by_email.text.toString())
                findNavController().navigate(
                    R.id.action_registerEmail_to_registerPassword,
                    bundle
                )
            } else {
                search_by_email.error = "Invalid email"
            }
        }
        backBtn.setOnClickListener {
            activity?.onBackPressed()
        }
    }
}