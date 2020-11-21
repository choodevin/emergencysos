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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.emergency.sosalert.R
import com.emergency.sosalert.SettingsActivity
import com.emergency.sosalert.admin.AdminContainer
import com.emergency.sosalert.discussion.MyPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_user_profile.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.regex.Pattern

@Suppress("DEPRECATION")
class UserProfile : Fragment() {
    private lateinit var nameData: String
    private val uid = FirebaseAuth.getInstance().uid ?: ""
    private val PERM_REQUEST = 3
    private var newimage: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profilepic.clipToOutline = true

        goToSettings.setOnClickListener {
            startActivity(Intent(context, SettingsActivity::class.java))
            activity?.finish()
        }

        viewMyPost.setOnClickListener {
            startActivity(Intent(context, MyPost::class.java))
        }

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val postReference = FirebaseFirestore.getInstance()
        postReference.collection("user").document(uid).get().addOnSuccessListener {
            if (it != null) {
                editTextName.setText(it.data?.get("name").toString())
                editTextDob.setText(it.data?.get("dob").toString())
                editTextContact.setText(it.data?.get("contact").toString())
                editTextGender.setText(it.data?.get("gender").toString())
                editTextName.isEnabled = false
                editTextDob.isEnabled = false
                editTextContact.isEnabled = false
                displayViews()
                if (it.data?.get("isAdmin").toString().compareTo("yes") == 0) {
                    toAdminButton.visibility = View.VISIBLE
                } else {
                    toAdminButton.visibility = View.GONE
                }
            }
        }

        toAdminButton.setOnClickListener {
            startActivity(Intent(context, AdminContainer::class.java))
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

        toAdminButton.setOnClickListener {
            startActivity(Intent(context, AdminContainer::class.java))
        }

    }

    private fun editProfile() {
        profileTitle.text = "Edit"
        editTextName.isEnabled = true
        editTextContact.isEnabled = true
        editTextName.requestFocus()

        goToSettings.visibility = View.GONE
        viewMyPost.visibility = View.GONE
        toAdminButton.visibility = View.GONE
        editProfBtn.visibility = View.GONE
        cfmButton.visibility = View.VISIBLE
        cancelBtn.visibility = View.VISIBLE
        editPictureBtn.visibility = View.VISIBLE

        cfmButton.setOnClickListener { submitData() }

        cancelBtn.setOnClickListener {
            val uid = FirebaseAuth.getInstance().uid ?: ""
            val postReference = FirebaseFirestore.getInstance()
            postReference.collection("user").document(uid).get().addOnSuccessListener {
                if (it != null) {
                    editTextName.setText(it.data?.get("name").toString())
                    editTextContact.setText(it.data?.get("contact").toString())
                    editTextName.isEnabled = false
                }
            }
            val storageRef = FirebaseStorage.getInstance().reference
            storageRef.child("profilepicture/$uid").downloadUrl.addOnSuccessListener {
                if (profilepic != null) {
                    Glide.with(requireContext()).load(it).into(profilepic)
                }
            }
            profileTitle.text = "Your Profile"
            FirebaseFirestore.getInstance().collection("user").document(uid).get()
                .addOnSuccessListener {
                    if (it.data!!["isAdmin"].toString() == "yes") {
                        toAdminButton.visibility = View.VISIBLE
                    }
                }
            goToSettings.visibility = View.VISIBLE
            editProfBtn.visibility = View.VISIBLE
            viewMyPost.visibility = View.VISIBLE
            cancelBtn.visibility = View.GONE
            cfmButton.visibility = View.GONE
            editPictureBtn.visibility = View.GONE
            editTextName.isEnabled = false
            editTextDob.isEnabled = false
            editTextContact.isEnabled = false
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
        val contactPattern = "^(\\+?6?01)[0-46-9]-*[0-9]{7,8}$"
        nameData = editTextName.text.toString()
        val contact = editTextContact.text.toString()
        when {
            nameData.isEmpty() -> {
                Toast.makeText(
                    requireActivity(),
                    "Please do not leave it empty",
                    Toast.LENGTH_SHORT
                ).show()
                editTextName.requestFocus()
                return
            }
            contact.isEmpty() -> {
                Toast.makeText(
                    requireActivity(),
                    "Please do not leave it empty",
                    Toast.LENGTH_SHORT
                ).show()
                editTextName.requestFocus()
                return
            }
            !Pattern.matches(contactPattern, contact) -> {
                Toast.makeText(context, "Invalid contact number", Toast.LENGTH_SHORT).show()
                editTextContact.requestFocus()
                return
            }
            else -> {
                val uid = FirebaseAuth.getInstance().uid ?: ""
                val ref = FirebaseFirestore.getInstance().collection("user").document(uid)
                ref.update(
                    "name", nameData,
                    "contact", contact
                ).addOnSuccessListener {
                    Toast.makeText(
                        requireActivity(),
                        "Info Successfully updated",
                        Toast.LENGTH_SHORT
                    ).show()

                    if (newimage != null) {
                        val newStorageReference =
                            FirebaseStorage.getInstance().getReference("/profilepicture/$uid")
                        val uri = Uri.parse(newimage.toString())

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val source =
                                ImageDecoder.createSource(requireActivity().contentResolver, uri)
                            val bitmap = ImageDecoder.decodeBitmap(source)
                            newStorageReference.putBytes(finalByteArray(bitmap))
                        } else {
                            val bitmap =
                                MediaStore.Images.Media.getBitmap(
                                    requireActivity().contentResolver,
                                    uri
                                )
                            newStorageReference.putBytes(finalByteArray(bitmap))
                        }
                    }
                }
                profileTitle.text = "Your Profile"
                goToSettings.visibility = View.VISIBLE
                editProfBtn.visibility = View.VISIBLE
                cancelBtn.visibility = View.GONE
                cfmButton.visibility = View.GONE
                editPictureBtn.visibility = View.GONE
                viewMyPost.visibility = View.VISIBLE
                editTextName.isEnabled = false
                editTextDob.isEnabled = false
                editTextContact.isEnabled = false
                FirebaseFirestore.getInstance().collection("user").document(uid).get()
                    .addOnSuccessListener {
                        if (it.data!!["isAdmin"].toString() == "yes") {
                            toAdminButton.visibility = View.VISIBLE
                        }
                    }
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
                        MediaStore.Images.Media.getBitmap(
                            requireActivity().contentResolver,
                            newimage
                        )
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

    private fun displayViews() {
        editProfBtn.visibility = View.VISIBLE
        goToSettings.visibility = View.VISIBLE
        profilepic.visibility = View.VISIBLE
        dobLabel.visibility = View.VISIBLE
        contactLabel.visibility = View.VISIBLE
        nameLabel.visibility = View.VISIBLE
        editTextName.visibility = View.VISIBLE
        editTextContact.visibility = View.VISIBLE
        editTextDob.visibility = View.VISIBLE
        viewMyPost.visibility = View.VISIBLE
        genderLabel.visibility = View.VISIBLE
        editTextGender.visibility = View.VISIBLE
        profileLoading.visibility = View.GONE
        FirebaseFirestore.getInstance().collection("user").document(uid).get()
            .addOnSuccessListener {
                if (it.data!!["isAdmin"].toString() == "yes") {
                    toAdminButton.visibility = View.VISIBLE
                }
            }
    }

}