package com.geektanmoy.imagecropper.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.geektanmoy.imagecropper.AppUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhotoFragment : Fragment() {

    private var cameraImageUri: Uri? = null

    override fun onResume() {
        super.onResume()

        if (hasPermission(requireContext(), getPermission())) {
            openCamera()
        } else {
            requestPermission()
        }
    }

    private fun hasPermission(context: Context, permissions: Array<String>) = permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getPermission(): Array<String> {
        val permissionsList = ArrayList<String>()
        permissionsList.add(Manifest.permission.CAMERA)
        permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        return permissionsList.toTypedArray()
    }

    private fun requestPermission() {
        permissionLauncher.launch(getPermission())
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                openCamera()
            } else {
                requestPermission()
            }
        }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile = createImageFile()
        cameraImageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
        startForCameraResult.launch(intent)
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault()).format(Date())
        return File.createTempFile("IMG_${timestamp}", ".jpg", requireContext().cacheDir)
    }

    private val startForCameraResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                cameraImageUri?.let { uri ->
                    AppUtils.loge( "Uri : $uri")
                    findNavController().navigate(
                        PhotoFragmentDirections.actionPhotoFragmentToCropFragment(
                            uri.toString()
                        )
                    )
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                requireActivity().finish()
            }
        }
}