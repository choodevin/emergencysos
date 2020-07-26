package com.emergency.sosalert.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.emergency.sosalert.R
import kotlinx.android.synthetic.main.fragment_register_name.*

class RegisterName : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_name, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        continueBtn.setOnClickListener {
            findNavController().navigate(R.id.action_registerName_to_registerPicture)
        }
        backBtn.setOnClickListener {
            activity?.onBackPressed()
        }
    }
}