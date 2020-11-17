package com.emergency.sosalert.login

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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
            if (Patterns.EMAIL_ADDRESS.matcher(input_email.text).matches()) {
                val bundle = Bundle()
                bundle.putString("email", input_email.text.toString())
                findNavController().navigate(
                    R.id.action_registerEmail_to_registerPassword,
                    bundle
                )
            } else {
                input_email.error = "Invalid email"
            }
        }
        input_email.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                continueBtn.performClick()
                closeKeyboard()
                return@setOnEditorActionListener true
            } else {
                return@setOnEditorActionListener false
            }
        }

        backBtn.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun closeKeyboard() {
        val view = activity?.currentFocus
        if (view != null) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}