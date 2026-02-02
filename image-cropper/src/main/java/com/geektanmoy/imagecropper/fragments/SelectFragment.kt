package com.geektanmoy.imagecropper.fragments

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.geektanmoy.imagecropper.utils.AppUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SelectFragment : Fragment() {

    private var cameraImageUri: Uri? = null
    private var action: String = ""
    private var mimeType: String = ""

    override fun onResume() {
        super.onResume()

        try {
            AppUtils.loge("Action : ${requireActivity().intent.getStringExtra("ACTION")}")
            if (requireActivity().intent.hasExtra("ACTION")) {
                action = requireActivity().intent.getStringExtra("ACTION").toString()
                mimeType = requireActivity().intent.getStringExtra("MIME").toString()
                if (action == "C") {
                    if (hasPermission(requireContext(), getPermission("C"))) {
                        openCamera()
                    } else {
                        requestPermission()
                    }
                } else if (action == "G") {
                    if (hasPermission(requireContext(), getPermission("G"))) {
                        openGallery()
                    } else {
                        requestPermission()
                    }
                }
            } else {
                AppUtils.loge("No Action")
                requireActivity().finish()
            }
        } catch (e: Exception) {
            AppUtils.loge(e.message.toString())
            requireActivity().finish()
        }
    }

    private fun hasPermission(context: Context, permissions: Array<String>) = permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getPermission(action: String): Array<String> {
        val permissionsList = ArrayList<String>()
        if (action == "C") {
            permissionsList.add(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                permissionsList.add(Manifest.permission.READ_MEDIA_IMAGES)
                permissionsList.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsList.add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else if (action == "G") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                permissionsList.add(Manifest.permission.READ_MEDIA_IMAGES)
                permissionsList.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsList.add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        AppUtils.loge("Permission List Size : ${permissionsList.size}")
        return permissionsList.toTypedArray()
    }

    private fun requestPermission() {
        permissionLauncher.launch(getPermission(action))
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                if (action == "C") {
                    openCamera()
                } else if (action == "G") {
                    openGallery()
                }
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
        cameraResultLauncher.launch(intent)
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault()).format(Date())
        //return File.createTempFile("IMG_${timestamp}", ".jpg", requireContext().cacheDir)
        return File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "IMG_${timestamp}.jpg"
        )
    }

    private val cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                cameraImageUri?.let { uri ->
                    AppUtils.loge("Uri : $uri")

                    findNavController().navigate(
                        SelectFragmentDirections.actionSelectFragmentToPreviewFragment(
                            uri.toString(),
                            action,
                            false,
                            null
                        )
                    )
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                requireActivity().finish()
            }
        }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        val mimeTypes = if (mimeType.isNotEmpty()) {
            if (mimeType == "IP") {
                arrayOf("image/*", "application/pdf")
            } else {
                arrayOf("image/*")
            }
        } else {
            arrayOf("image/*")
        }
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        galleryResultLauncher.launch(intent)
    }

    private val galleryResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                if (result.data != null) {
                    val uri: Uri? = result.data?.data
                    AppUtils.loge("Uri : $uri")
                    if (uri != null) {
                        val file: File = AppUtils.fileFromContentUri(requireContext(), uri)
                        AppUtils.loge("File : $file")
                        AppUtils.loge("File Mime Type : ${AppUtils.getMimeType(file.extension)}")
                        AppUtils.loge("File Size : ${file.length()}Bytes")
                        AppUtils.loge("File Size : ${file.length().div(1024)}KB")
                        AppUtils.loge("File Size : ${file.length().div((1024 * 1024))}MB")

                        if (file.extension.lowercase() == "pdf") {
                            if (file.length() > (1024 * 1024)) { //Less than 1MB
                                Toast.makeText(
                                    requireContext(), "File size is more than 1MB",
                                    Toast.LENGTH_LONG
                                )
                            } else {
                                Intent().also { intent ->
                                    intent.putExtra("imageUri", uri.toString())
                                    requireActivity().setResult(RESULT_OK, intent)
                                    requireActivity().finish()
                                }
                            }
                        } else {
                            findNavController().navigate(
                                SelectFragmentDirections.actionSelectFragmentToPreviewFragment(
                                    uri.toString(),
                                    action,
                                    false,
                                    null
                                )
                            )
                        }
                    }
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                requireActivity().finish()
            }
        }
}