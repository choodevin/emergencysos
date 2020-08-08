package com.emergency.sosalert.profile

import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.emergency.sosalert.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_register_name.*

class RegisterName : Fragment() {
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_name, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        var profileName = ""

        if (auth.currentUser != null) {
            profileName = auth.currentUser!!.displayName as String
        }

        nameInput.setText(profileName)
        continueBtn.setOnClickListener {
            if (nameInput.text.isBlank() || nameInput.text.isEmpty()) {
                nameInput.error = "Please fill in your name!"
            } else {
                arguments?.putString("name", nameInput.text.toString())
                findNavController().navigate(R.id.action_registerName_to_registerPicture, arguments)
            }
        }
        backBtn.setOnClickListener {
            activity?.onBackPressed()
        }
    }
}