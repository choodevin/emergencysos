package com.emergency.sosalert.login

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.emergency.sosalert.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_register_contact_d_o_b.*
import kotlinx.coroutines.selects.select
import java.text.DateFormatSymbols
import java.util.*
import java.util.regex.Pattern
import javax.xml.datatype.DatatypeConstants.MONTHS

@Suppress("NAME_SHADOWING")
class RegisterContactDOB : Fragment() {
    private var contact = ""
    private var dob = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_contact_d_o_b, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backBtn.setOnClickListener {
            activity?.onBackPressed()
        }

        continueBtn.setOnClickListener {
            arguments?.putString("contact", contact)
            if (ageValid()) {
                arguments?.putString("dob", dob)
                findNavController().navigate(
                    R.id.action_registerContactDOB_to_registerGender,
                    arguments
                )
            }
        }

        input_contact.addTextChangedListener(object : TextWatcher {
            private val contactPattern = "^(\\+?6?01)[0-46-9]-*[0-9]{7,8}$"
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (!Pattern.matches(contactPattern, input_contact.text.toString())) {
                    input_contact.error = "Invalid contact format"
                    continueBtn.isEnabled = false
                } else {
                    continueBtn.isEnabled = true
                    contact = input_contact.text.toString()
                }
            }
        })

        selectedDob.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(
                requireContext(),
                R.style.DatePickerDialogTheme,
                { _, year, month, day ->
                    val tempStr = "$day ${DateFormatSymbols().months[month]} $year"
                    selectedDob.text = tempStr
                    dob = tempStr
                },
                year,
                month,
                day
            )
            dpd.show()
        }
    }

    private fun ageValid(): Boolean {
        return if (selectedDob.text == "Select date of birth") {
            false
        } else {
            val age: Int
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val bornYear = dob.split(" ").toTypedArray()
            age = currentYear - bornYear[2].toInt()
            return if (age > 100) {
                Snackbar.make(
                    requireView(),
                    "Date of birth entered is invalid",
                    Snackbar.LENGTH_SHORT
                ).show()
                false
            } else {
                return if (age <= 12) {
                    Snackbar.make(
                        requireView(),
                        "Minimum age 12 is required!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    false
                } else {
                    return true
                }
            }
        }
    }
}