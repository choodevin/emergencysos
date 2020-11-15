package com.emergency.sosalert.discussion

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.emergency.sosalert.R
import com.emergency.sosalert.firebaseMessaging.NotificationData
import com.emergency.sosalert.firebaseMessaging.PushNotification
import com.emergency.sosalert.firebaseMessaging.RetrofitInstance
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_create_discussion.*
import kotlinx.android.synthetic.main.activity_discussion_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

@Suppress("DEPRECATION")
class CreateDiscussion : AppCompatActivity() {
    private var IMAGE_SELECT = 0
    private var SELECT_LOCATION = 1
    private var PERM_REQUEST = 3
    private var photoExist = false
    private var locationExist = false
    private lateinit var tempLocation: Any
    private lateinit var image: Uri
    private lateinit var auth: FirebaseAuth

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
            createDiscussionProgressBar.visibility = View.VISIBLE
            inputTitle.isEnabled = false
            inputDesc.isEnabled = false
            cameraBtn.isEnabled = false
            locationBtn.isEnabled = false
            completeBtn.isEnabled = false
            val title = inputTitle.text.toString()
            val descriptionText = inputDesc.text.toString()
            val discussion = Discussion()
            val firestore = FirebaseFirestore.getInstance()

            if (title.isEmpty() || descriptionText.isEmpty() || !photoExist || !locationExist) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Everything must be filled in or selected!",
                    Snackbar.LENGTH_LONG
                ).show()
                createDiscussionProgressBar.visibility = View.GONE
                inputTitle.isEnabled = true
                inputDesc.isEnabled = true
                cameraBtn.isEnabled = true
                locationBtn.isEnabled = true
                completeBtn.isEnabled = true
                return@setOnClickListener
            }

            val temp1 = tempLocation.toString()
            val location = temp1.split(",").toTypedArray()

            auth = FirebaseAuth.getInstance()
            discussion.title = title
            discussion.description = descriptionText
            discussion.ownerUid = auth.currentUser!!.uid
            discussion.latitude = location[0]
            discussion.longitude = location[1]

            val discHashMap = hashMapOf(
                "title" to discussion.title,
                "description" to discussion.description,
                "uploadtime" to discussion.uploadtime,
                "ownerUid" to discussion.ownerUid,
                "latitude" to discussion.latitude,
                "longitude" to discussion.longitude,
                "status" to "pending"
            )

            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder
                .setTitle("Discussion created successfully!")
                .setMessage("Your discussion will be pending for approval from admin")
            dialogBuilder.setPositiveButton("Done") { _, _ ->
                finish()
            }
            dialogBuilder.setOnDismissListener {
                finish()
            }

            val dialog = dialogBuilder.create()

            dialog.setOnShowListener {
                dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            }

            firestore.collection("discussion").add(discHashMap).addOnSuccessListener { new ->
                val storageReference =
                    FirebaseStorage.getInstance().getReference("/discussionPicture/${new.id}")
                val uploadTask = storageReference.putFile(image)

                firestore.collection("discussion").document(new.id).update("commentgroup", new.id)

                uploadTask.continueWithTask { _ ->
                    storageReference.downloadUrl.addOnSuccessListener {
                        val imageUrlHash = hashMapOf("imageUrl" to it.toString())
                        firestore.collection("discussion").document(new.id)
                            .set(imageUrlHash, SetOptions.merge())
                        dialog.show()

                        FirebaseFirestore.getInstance().collection("user")
                            .whereEqualTo("isAdmin", "yes").get().addOnSuccessListener { admin ->
                                for (a in admin) {
                                    Log.e("SEND NOTIF", "ADMIN FOUND")
                                    PushNotification(
                                        NotificationData(
                                            "Discussion Apporval",
                                            "New dicussion(s) are ready to be reviewed!",
                                            "222",
                                            "222",
                                            "no"
                                        ),
                                        a.data["token"].toString()
                                    ).also { notif ->
                                        sendNotification(notif)
                                    }

                                }
                            }
                    }
                }
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
                        ImageDecoder.createSource(this.contentResolver, image)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    IMAGE_SELECT = 1
                    selectedImage.setImageBitmap(bitmap)
                    selectedImage.background = null
                    cameraBtn.background =
                        ContextCompat.getDrawable(this, R.drawable.button_edit_photo)
                    photoExist = true
                } else {
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, image)
                    IMAGE_SELECT = 1
                    selectedImage.setImageBitmap(bitmap)
                    selectedImage.background = null
                    cameraBtn.background =
                        ContextCompat.getDrawable(this, R.drawable.button_edit_photo)
                    photoExist = true
                }
            } catch (v: IOException) {
                Log.v(ContentValues.TAG, v.message.toString())
            }
        } else if (requestCode == SELECT_LOCATION && resultCode == RESULT_OK) {
            if (data!!.extras!!.get("selectedLocation") != null) {
                tempLocation = data.extras!!.get("selectedLocation")!!
                locationBtn.background =
                    ContextCompat.getDrawable(this, R.drawable.button_edit_location)
                locationExist = true
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postNotification(notification)
                if (response.isSuccessful) {
                    Log.d(ContentValues.TAG, "Response: ${Gson().toJson(response)}")
                } else {
                    Log.e(ContentValues.TAG, response.errorBody().toString())
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, e.toString())
            }
        }
}