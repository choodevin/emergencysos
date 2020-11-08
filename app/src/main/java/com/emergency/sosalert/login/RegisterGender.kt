package com.emergency.sosalert.login

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.emergency.sosalert.MainActivity
import com.emergency.sosalert.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_register_gender.*
import java.io.ByteArrayOutputStream

@Suppress("DEPRECATION")
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
            closeKeyboard()
            buttonToggle(0)
            progressBar.visibility = View.VISIBLE
            backBtn.visibility = View.GONE
            auth = FirebaseAuth.getInstance()

            if (maleBtn.isChecked) {
                gender = "male"
            } else if (femaleBtn.isChecked) {
                gender = "female"
            }
            try {
                age = inputAge.text.toString().toInt()
            } catch (e: Exception) {
                Snackbar.make(
                    requireView(),
                    "Please do not leave this empty/Only input numbers.",
                    Snackbar.LENGTH_LONG
                ).show()
                inputAge.requestFocus()
                return@setOnClickListener
            }
            if (age >= 100 || age <= 0) {
                inputAge.error = "Invalid age"
            } else {
                if (arguments?.get("email") == null) {
                    name = auth.currentUser!!.displayName!!
                    insertDetails(auth.currentUser!!.email.toString())
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
                            buttonToggle(1)
                            progressBar.visibility = View.GONE
                            backBtn.visibility = View.VISIBLE
                        } else {
                            insertDetails(email)
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

    private fun buttonToggle(mode: Int) {
        if (mode == 1) {
            continueBtn.isEnabled = true
            maleBtn.isEnabled = true
            femaleBtn.isEnabled = true
            inputAge.isEnabled = true
        } else if (mode == 0) {
            continueBtn.isEnabled = false
            maleBtn.isEnabled = false
            femaleBtn.isEnabled = false
            inputAge.isEnabled = false
        }
    }

    private fun insertDetails(email: String) {
        val user = User()
        user.name = name
        user.gender = gender
        user.age = age
        val userId = auth.uid ?: ""
        val fireStore = FirebaseFirestore.getInstance()
        val userHashMap = hashMapOf(
            "name" to user.name,
            "gender" to user.gender,
            "age" to user.age,
            "email" to email,
            "allowTracking" to false,
            "isAdmin" to "no",
            "latitude" to 0,
            "longitude" to 0
        )

        fireStore.collection("user").document(userId).set(userHashMap)
        fireStore.collection("user").document(userId).update("isAdmin", "no")

        val storageReference =
            FirebaseStorage.getInstance().getReference("/profilepicture/$userId")

        val uri = Uri.parse(arguments?.getString("photo").toString())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source =
                ImageDecoder.createSource(requireActivity().contentResolver, uri)
            val bitmap = ImageDecoder.decodeBitmap(source)
            storageReference.putBytes(finalByteArray(bitmap))
        } else {
            val bitmap =
                MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
            storageReference.putBytes(finalByteArray(bitmap))
        }
    }

    private fun finalByteArray(bitmap: Bitmap): ByteArray {
        val baOS = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baOS)
        return baOS.toByteArray()
    }

    private fun closeKeyboard() {
        val view = activity?.currentFocus
        if (view != null) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}