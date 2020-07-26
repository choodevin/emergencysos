package com.emergency.sosalert.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.emergency.sosalert.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_register_gender.*

class RegisterGender : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_gender, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        continueBtn.setOnClickListener {
            if (genderGroup.checkedRadioButtonId == -1) {
                Snackbar.make(
                    it,
                    "Please select one of the gender, you can't be genderless.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
        backBtn.setOnClickListener {
            activity?.onBackPressed()
        }
    }
}