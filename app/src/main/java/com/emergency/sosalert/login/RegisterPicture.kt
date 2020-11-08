package com.emergency.sosalert.login

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.ContentValues.TAG
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
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.emergency.sosalert.R
import com.google.firebase.auth.FirebaseAuth
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_register_picture.*
import java.io.IOException


@Suppress("DEPRECATION")
class RegisterPicture : Fragment() {
    private var image: Uri? = null
    private var PROFILE_SETTED = 0
    private var REGISTER_ABORT = false
    private var GOOGLE_REGISTER = false
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

        if (arguments?.getString("email").isNullOrEmpty()) {
            GOOGLE_REGISTER = true
        }

        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder
            .setTitle("Profile picture not setted")
            .setMessage("Not setting a profile picture makes it harder to recognize you!")
        dialogBuilder.setNegativeButton("Return") { _, _ ->
        }
        val dialog = dialogBuilder.create()

        val dialogBuilder2 = AlertDialog.Builder(requireContext())
        dialogBuilder2
            .setTitle("Cancel registration?")
            .setMessage("Are you sure you want to stop right here?")
        dialogBuilder2.setPositiveButton("Yes") { _, _ ->
            val auth = FirebaseAuth.getInstance()
            auth.currentUser?.delete()
            REGISTER_ABORT = true
            activity?.onBackPressed()
        }
        dialogBuilder2.setNegativeButton("Return") { _, _ ->
        }

        val dialog2 = dialogBuilder2.create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark))
        }

        dialog2.setOnShowListener {
            dialog2.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark))
            dialog2.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark))
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

        if (GOOGLE_REGISTER) {
            activity?.onBackPressedDispatcher?.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (REGISTER_ABORT) {
                            super.remove()
                            activity?.onBackPressed()
                        } else {
                            dialog2.show()
                        }
                    }
                })
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
                startCameraAndCrop()
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

    private fun toNext() {
        if (PROFILE_SETTED == 1) {
            arguments?.putString("photo", image.toString())
        }
        findNavController().navigate(R.id.action_registerPicture_to_registerGender, arguments)
    }

    private fun startCameraAndCrop() {
        CropImage.activity()
            .setAspectRatio(1, 1)
            .setCropShape(CropImageView.CropShape.OVAL)
            .setFixAspectRatio(true)
            .start(requireContext(), this)
    }
}