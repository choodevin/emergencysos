package com.emergency.sosalert.discussion

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.emergency.sosalert.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_create_discussion.*
import java.io.IOException

class CreateDiscussion : AppCompatActivity() {
    private var IMAGE_SELECT = 0
    private var LOCATION_SELECT = 0
    private var SELECT_LOCATION = 1
    private var PERM_REQUEST = 3
    private lateinit var image: Uri
    private lateinit var location: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_discussion)

        closeButton.setOnClickListener {
            this.finish()
        }

        cameraBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CAMERA), PERM_REQUEST
                )
            } else {
                selectImage()
            }
        }

        locationBtn.setOnClickListener {
            startActivityForResult(Intent(this, SelectLocation::class.java), SELECT_LOCATION)
        }

        completeBtn.setOnClickListener {
            val title = inputTitle.text.toString()
            val description = inputDesc.text.toString()
            val discussion = Discussion(title, description)
            val firestore = FirebaseFirestore.getInstance()
            val discHashMap = hashMapOf(
                "title" to discussion.title,
                "description" to discussion.description
            )

            firestore.collection("discussion").add(discHashMap).addOnSuccessListener { new ->
                val storageReference =
                    FirebaseStorage.getInstance().getReference("/discussionPicture/${new.id}")
                storageReference.putFile(image)
            }

        }
    }

    private fun selectImage() {
        CropImage.activity()
            .start(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERM_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            image = result.uri
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source =
                        ImageDecoder.createSource(this.contentResolver, image!!)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    IMAGE_SELECT = 1
                    selectedImage.setImageBitmap(bitmap)
                    selectedImage.background = null
                    noImageText.visibility = View.INVISIBLE
                } else {
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, image)
                    IMAGE_SELECT = 1
                    selectedImage.setImageBitmap(bitmap)
                    selectedImage.background = null
                    noImageText.visibility = View.INVISIBLE
                }
            } catch (v: IOException) {
                Log.v(ContentValues.TAG, v.message.toString())
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}