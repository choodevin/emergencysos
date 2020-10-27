package com.emergency.sosalert.main

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.emergency.sosalert.LoginActivity
import com.emergency.sosalert.R
import com.emergency.sosalert.firebaseMessaging.FirebaseService
import com.emergency.sosalert.login.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_register_picture.*
import kotlinx.android.synthetic.main.fragment_user_profile.*
import java.io.ByteArrayOutputStream
import java.io.IOException

class UserProfile : Fragment() {
    private var auth = FirebaseAuth.getInstance()
    private lateinit var nameData: String
    private var ageData: Int = 0
    private lateinit var genderData: String
    private val uid = FirebaseAuth.getInstance().uid ?: ""
    private val PERM_REQUEST = 3
    private var image: Uri? = null
    private var newimage: Uri? = null

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
            FirebaseFirestore.getInstance().collection("user").document(uid)
                .update("token", "empty")
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
        editPictureBtn.visibility = View.VISIBLE
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
            val storageRef = FirebaseStorage.getInstance().reference
            storageRef.child("profilepicture/$uid").downloadUrl.addOnSuccessListener {
                val uri = it
                if (profilepic != null) {
                    Glide.with(requireContext()).load(it).into(profilepic)
                }
            }
            editProfBtn.visibility = View.VISIBLE
            cancelBtn.visibility = View.GONE
            cfmButton.visibility = View.GONE
            editPictureBtn.visibility = View.GONE
            logoutBtn.visibility = View.VISIBLE
            editTextName.isEnabled = false
            editTextAge.isEnabled = false
        }
        editPictureBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.CAMERA), PERM_REQUEST
                )
            } else {
                startCameraAndCrop()
            }
        }
    }
    private fun submitData() {
        nameData = editTextName.text.toString()
        try {
            ageData = Integer.parseInt(editTextAge.text.toString())
        }catch(e:Exception){
            Toast.makeText(requireActivity(), "Please don't leave the age empty/ Only put integer value", Toast.LENGTH_SHORT)
        }
        when {
            nameData.isEmpty() -> {
                Toast.makeText(requireActivity(), "Please do not leave it empty", Toast.LENGTH_SHORT)
                editTextName.requestFocus()
                return
            }
            ageData in 101..0 -> {
                Toast.makeText(requireActivity(), "Please input a correct age range", Toast.LENGTH_SHORT)
                editTextAge.requestFocus()
                return
            }
            else -> {
                val uid = FirebaseAuth.getInstance().uid ?: ""
                val ref = FirebaseFirestore.getInstance().collection("user").document(uid)
                ref.update(
                    "name", nameData,
                    "age", ageData
                ).addOnSuccessListener {
                    Toast.makeText(requireActivity(), "Info Successfully updated", Toast.LENGTH_SHORT)
                    val storageReference =
                        FirebaseStorage.getInstance().getReference("/profilepicture/$uid")
                    storageReference.delete()
                    val newStorageReference =
                        FirebaseStorage.getInstance().getReference("/profilepicture/$uid")
                    val uri = Uri.parse(newimage.toString())

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source =
                            ImageDecoder.createSource(requireActivity().contentResolver, uri)
                        var bitmap = ImageDecoder.decodeBitmap(source)
                        newStorageReference.putBytes(finalByteArray(bitmap))
                    } else {
                        var bitmap =
                            MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                        newStorageReference.putBytes(finalByteArray(bitmap))
                    }
                }
                editProfBtn.visibility = View.VISIBLE
                cancelBtn.visibility = View.GONE
                cfmButton.visibility = View.GONE
                editPictureBtn.visibility = View.GONE
                logoutBtn.visibility = View.VISIBLE
                editTextName.isEnabled = false
                editTextAge.isEnabled = false
            }
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            newimage = result.uri
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source =
                        ImageDecoder.createSource(requireActivity().contentResolver, newimage!!)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    profilepic.setImageBitmap(bitmap)
                } else {
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, newimage)
                    profilepic.setImageBitmap(bitmap)
                }
            } catch (v: IOException) {
                Log.v(ContentValues.TAG, v.message.toString())
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERM_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraAndCrop()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    private fun startCameraAndCrop() {
        CropImage.activity()
            .setAspectRatio(1, 1)
            .setCropShape(CropImageView.CropShape.OVAL)
            .setFixAspectRatio(true)
            .start(requireContext(), this)
    }
    private fun finalByteArray(bitmap: Bitmap): ByteArray {
        val baOS = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baOS)
        return baOS.toByteArray()
    }

}