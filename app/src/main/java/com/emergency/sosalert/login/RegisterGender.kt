package com.emergency.sosalert.login

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
import androidx.fragment.app.Fragment
import com.emergency.sosalert.MainActivity
import com.emergency.sosalert.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_register_gender.*
import kotlinx.android.synthetic.main.fragment_register_gender.backBtn
import kotlinx.android.synthetic.main.fragment_register_gender.continueBtn
import java.io.ByteArrayOutputStream

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
            buttonToggle(0)
            progressBar.visibility = View.VISIBLE
            backBtn.visibility = View.GONE
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
                            buttonToggle(1)
                            progressBar.visibility = View.GONE
                            backBtn.visibility = View.VISIBLE
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source =
                    ImageDecoder.createSource(requireActivity().contentResolver, uri)
                var bitmap = ImageDecoder.decodeBitmap(source)
                storageReference.putBytes(finalByteArray(bitmap))
            } else {
                var bitmap =
                    MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                storageReference.putBytes(finalByteArray(bitmap))
            }
        }
    }

    private fun finalByteArray(bitmap: Bitmap): ByteArray {
        val baOS = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baOS)
        return baOS.toByteArray()
    }
}