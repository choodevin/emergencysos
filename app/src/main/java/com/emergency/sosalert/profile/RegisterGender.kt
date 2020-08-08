package com.emergency.sosalert.profile

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.emergency.sosalert.MainActivity
import com.emergency.sosalert.R
import com.emergency.sosalert.entity.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.shawnlin.numberpicker.NumberPicker
import com.shawnlin.numberpicker.NumberPicker.OnScrollListener.SCROLL_STATE_IDLE
import kotlinx.android.synthetic.main.fragment_register_gender.*
import java.lang.Exception

class RegisterGender : Fragment() {
    private lateinit var auth: FirebaseAuth
    private var gender = ""
    private var age = 0
    private var name = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_gender, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        continueBtn.setOnClickListener {
            auth = FirebaseAuth.getInstance()
            if (maleBtn.isChecked) {
                gender = "male"
            } else if (femaleBtn.isChecked) {
                gender = "female"
            }

            age = inputAge.text.toString().toInt()

            if (age >= 100 || age <= 0) {
                inputAge.error = "Invalid age"
            } else {
                if (arguments?.get("email") == null) {
                    name = auth.currentUser!!.displayName!!
                    insertDetails()
                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                    activity?.finish()
                } else {
                    val email = arguments?.get("email").toString()
                    val password = arguments?.get("password").toString()
                    name = arguments?.getString("name")!!

                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (!it.isSuccessful) {
                            Snackbar.make(
                                requireView(),
                                "Register failed, please check if the email is registered or try again.",
                                Snackbar.LENGTH_LONG
                            ).show()
                        } else {
                            insertDetails()
                            startActivity(Intent(requireActivity(), MainActivity::class.java))
                            activity?.finish()
                        }
                    }
                }
            }
        }
        backBtn.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun insertDetails() {
        val user =
            User(name, gender, age)
        val userId = auth.uid ?: ""
        val fireStore = FirebaseFirestore.getInstance()
        val userHashMap = hashMapOf(
            "name" to user.name,
            "gender" to user.gender,
            "age" to user.age
        )

        fireStore.collection("user").document(userId).set(userHashMap)

        if (arguments?.getString("photo").toString() != "none") {
            val storageReference =
                FirebaseStorage.getInstance().getReference("/profilepicture/$userId")
            val uri = Uri.parse(arguments?.getString("photo").toString())
            storageReference.putFile(uri)
        }
    }
}