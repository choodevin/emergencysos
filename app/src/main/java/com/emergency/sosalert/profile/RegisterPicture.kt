package com.emergency.sosalert.profile

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.emergency.sosalert.R
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_register_picture.*
import java.io.IOException


class RegisterPicture : Fragment() {
    private var image: Uri? = null
    private var PROFILE_SETTED = 0
    private val PERM_REQUEST = 3
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_picture, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profileImage.clipToOutline = true

        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder
            .setTitle("Are you sure?")
            .setMessage("Not setting a profile picture makes the others harder to recognize you!")
        dialogBuilder.setPositiveButton("Skip anyway") { _, _ ->
            toNext()
        }
        dialogBuilder.setNegativeButton("Return") { _, _ ->
        }
        val dialog = dialogBuilder.create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(resources.getColor(R.color.colorPrimaryDark))
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(resources.getColor(R.color.colorPrimaryDark))
        }

        continueBtn.setOnClickListener {
            if (PROFILE_SETTED == 0) {
                dialog.show()
            } else {
                toNext()
            }
        }

        backBtn.setOnClickListener {
            activity?.onBackPressed()
        }

        skipText.setOnClickListener {
            dialog.show()
        }

        cameraBtn.setOnClickListener {
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
                CropImage.activity()
                    .setAspectRatio(1, 1)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setFixAspectRatio(true)
                    .start(requireContext(), this)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            image = result.uri
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source =
                        ImageDecoder.createSource(requireActivity().contentResolver, image!!)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    PROFILE_SETTED = 1
                    profileImage.setImageBitmap(bitmap)
                } else {
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, image)
                    PROFILE_SETTED = 1
                    profileImage.setImageBitmap(bitmap)
                }
            } catch (v: IOException) {
                Log.v(TAG, v.message.toString())
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun toNext() {
        if (PROFILE_SETTED == 1) {
            arguments?.putString("photo", image.toString())
        } else {
            arguments?.putString("photo", "none")
        }
        findNavController().navigate(R.id.action_registerPicture_to_registerGender, arguments)
    }
}