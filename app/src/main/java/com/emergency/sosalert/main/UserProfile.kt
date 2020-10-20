package com.emergency.sosalert.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.emergency.sosalert.LoginActivity
import com.emergency.sosalert.R
import com.emergency.sosalert.login.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_user_profile.*

class UserProfile : Fragment() {
    private var auth = FirebaseAuth.getInstance()
    private lateinit var nameData: String
    private var ageData: Int = 0
    private lateinit var genderData: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logoutBtn.setOnClickListener {
            auth.signOut()
            activity?.finish()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }
        (activity as AppCompatActivity).supportActionBar?.show()

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val postReference = FirebaseFirestore.getInstance()
        postReference.collection("user").document(uid).get().addOnSuccessListener {
                    if (it != null) {
                        editTextName.setText(it.data?.get("name").toString())
                        editTextAge.setText(it.data?.get("age").toString())
                        genderData = it.data?.get("gender").toString()
                        editTextName.isEnabled = false
                        editTextAge.isEnabled = false
                    }
                }

        val storageRef = FirebaseStorage.getInstance().reference
        storageRef.child("profilepicture/$uid").downloadUrl.addOnSuccessListener {
            val uri = it
            if (profilepic != null) {
                Glide.with(requireContext()).load(it).into(profilepic)
            }
            profilepic.setOnClickListener {
                if (uri != null) {
                    val i = Intent(Intent.ACTION_VIEW)
                    i.setDataAndType(uri, "image/jpeg")
                    startActivity(i)
                }
            }
    }
        editProfBtn.setOnClickListener { editProfile() }

}
    private fun editProfile() {
        editTextName.isEnabled = true
        editTextAge.isEnabled = true
        editTextName.requestFocus()

        editProfBtn.visibility = View.GONE
        cfmButton.visibility = View.VISIBLE
        cancelBtn.visibility = View.VISIBLE
        logoutBtn.visibility = View.GONE

        cfmButton.setOnClickListener { submitData() }

        cancelBtn.setOnClickListener {
            val uid = FirebaseAuth.getInstance().uid ?: ""
            val postReference = FirebaseFirestore.getInstance()
            postReference.collection("user").document(uid).get().addOnSuccessListener {
                if (it != null) {
                    editTextName.setText(it.data?.get("name").toString())
                    editTextAge.setText(it.data?.get("age").toString())
                    genderData = it.data?.get("gender").toString()
                    editTextName.isEnabled = false
                    editTextAge.isEnabled = false
                }
            }
            editProfBtn.visibility = View.VISIBLE
            cancelBtn.visibility = View.GONE
            cfmButton.visibility = View.GONE
            logoutBtn.visibility = View.VISIBLE
            editTextName.isEnabled = false
            editTextAge.isEnabled = false
        }
    }
    private fun submitData() {
        nameData = editTextName.text.toString()
        try {
            ageData = Integer.parseInt(editTextAge.text.toString())
        }catch(e:Exception){
            Toast.makeText(requireActivity(), "Please don't leave the age empty/ Only put integer value", Toast.LENGTH_SHORT)
        }
        if (nameData.isEmpty()) {
            editTextName.setText("Please do not leave it empty")
            editTextName.requestFocus()
            return
        } else if (ageData == 0) {
            editTextAge.setText("Please do not leave it empty")
            editTextAge.requestFocus()
            return
        } else {
            val uid = FirebaseAuth.getInstance().uid ?: ""
            val ref = FirebaseFirestore.getInstance().collection("user").document(uid)
            ref.update(
                "name", nameData,
                "age", ageData
            ).addOnSuccessListener {
                Toast.makeText(requireActivity(), "Info Successfully updated", Toast.LENGTH_SHORT)
            }

            editProfBtn.visibility = View.VISIBLE
            cancelBtn.visibility = View.GONE
            cfmButton.visibility = View.GONE
            logoutBtn.visibility = View.VISIBLE
            editTextName.isEnabled = false
            editTextAge.isEnabled = false
        }

    }

}