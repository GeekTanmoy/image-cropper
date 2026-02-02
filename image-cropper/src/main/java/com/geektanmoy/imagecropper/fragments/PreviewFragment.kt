package com.geektanmoy.imagecropper.fragments

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.geektanmoy.imagecropper.utils.AppUtils
import com.geektanmoy.imagecropper.databinding.FragmentPreviewBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.roundToInt


class PreviewFragment : Fragment() {

    private lateinit var binding: FragmentPreviewBinding
    private val args by navArgs<PreviewFragmentArgs>()
    private var cropUri: Uri? = null
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AppUtils.loge("Action : ${args.action}")

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        if (args.cropFlag) {
            AppUtils.loge("Crop Flag : ${args.cropFlag}")
            AppUtils.loge("Crop Uri : ${args.cropUri}")
            args.cropUri?.let { uri ->
                cropUri = uri.toUri()
                binding.ivImage.setImageURI(uri.toUri())
            }
        } else {
            AppUtils.loge("Crop Flag : ${args.cropFlag}")
            AppUtils.loge("Image Uri : ${args.imageUri}")
            args.imageUri.let { uri ->
                imageUri = uri.toUri()
                binding.ivImage.setImageURI(uri.toUri())
            }
        }

        args.imageUri.let { uriString ->
            //imageUri = uriString.toUri()
            imageUri?.let { uri ->
                val imageFile: File = AppUtils.fileFromContentUri(requireContext(), uri)
                AppUtils.loge("Image File : $imageFile")
                AppUtils.loge("Image File Mime Type : ${AppUtils.getMimeType(imageFile.extension)}")
                AppUtils.loge("Image File Size : ${imageFile.length()}Bytes")
                AppUtils.loge("Image File Size : ${imageFile.length().div(1024)}KB")
                AppUtils.loge("Image File Size : ${imageFile.length().div((1024 * 1024))}MB")
            }
        }

        binding.ivCrop.setOnClickListener {
            try {
                AppUtils.loge("Image Uri : $imageUri")
                imageUri?.let { uri ->
                    findNavController().navigate(
                        PreviewFragmentDirections.actionPreviewFragmentToCropFragment(
                            uri.toString(),
                            args.action
                        )
                    )
                }
            } catch (e: Exception) {
                AppUtils.loge("Error : ${e.message}")
            }
        }

        binding.btnUse.setOnClickListener {
            if (args.cropFlag) {
                cropUri?.let { uri ->
                    val imageFile: File = AppUtils.fileFromContentUri(requireContext(), uri)
                    AppUtils.loge("Image File : $imageFile")
                    AppUtils.loge("Image File Mime Type : ${AppUtils.getMimeType(imageFile.extension)}")
                    AppUtils.loge("Image File Size : ${imageFile.length()}Bytes")
                    AppUtils.loge("Image File Size : ${imageFile.length().div(1024)}KB")
                    AppUtils.loge("Image File Size : ${imageFile.length().div((1024 * 1024))}MB")

                    if (imageFile.length() > (1024 * 1024)) { //Less than 1MB
                        val compressedUri = compressImageTo1MB(requireContext(), uri)
                        compressedUri?.let { binding.ivImage.setImageURI(it) }

                        val compressedFile: File =
                            AppUtils.fileFromContentUri(requireContext(), compressedUri!!)
                        AppUtils.loge("Compressed Image File : $compressedFile")
                        AppUtils.loge(
                            "Compressed File Mime Type : ${
                                AppUtils.getMimeType(
                                    compressedFile.extension
                                )
                            }"
                        )
                        AppUtils.loge("Compressed File Size : ${compressedFile.length()}Bytes")
                        AppUtils.loge(
                            "Compressed File Size : ${
                                compressedFile.length().div(1024)
                            }KB"
                        )
                        AppUtils.loge(
                            "Compressed File Size : ${compressedFile.length().div((1024 * 1024))}MB"
                        )

                        compressedUri.let {
                            Intent().also { intent ->
                                intent.putExtra("imageUri", compressedUri.toString())
                                requireActivity().setResult(Activity.RESULT_OK, intent)
                                requireActivity().finish()
                            }
                        }
                    } else {
                        cropUri?.let {
                            Intent().also { intent ->
                                intent.putExtra("imageUri", it.toString())
                                requireActivity().setResult(Activity.RESULT_OK, intent)
                                requireActivity().finish()
                            }
                        }
                    }
                }
            } else {
                imageUri?.let { uri ->
                    val imageFile: File = AppUtils.fileFromContentUri(requireContext(), uri)
                    AppUtils.loge("Image File : $imageFile")
                    AppUtils.loge("Image File Mime Type : ${AppUtils.getMimeType(imageFile.extension)}")
                    AppUtils.loge("Image File Size : ${imageFile.length()}Bytes")
                    AppUtils.loge("Image File Size : ${imageFile.length().div(1024)}KB")
                    AppUtils.loge("Image File Size : ${imageFile.length().div((1024 * 1024))}MB")

                    if (imageFile.length() > (1024 * 1024)) { //Less than 1MB
                        val compressedUri = compressImageTo1MB(requireContext(), uri)
                        compressedUri?.let { binding.ivImage.setImageURI(it) }

                        val compressedFile: File =
                            AppUtils.fileFromContentUri(requireContext(), compressedUri!!)
                        AppUtils.loge("Compressed Image File : $compressedFile")
                        AppUtils.loge(
                            "Compressed File Mime Type : ${
                                AppUtils.getMimeType(
                                    compressedFile.extension
                                )
                            }"
                        )
                        AppUtils.loge("Compressed File Size : ${compressedFile.length()}Bytes")
                        AppUtils.loge(
                            "Compressed File Size : ${
                                compressedFile.length().div(1024)
                            }KB"
                        )
                        AppUtils.loge(
                            "Compressed File Size : ${compressedFile.length().div((1024 * 1024))}MB"
                        )

                        compressedUri.let {
                            Intent().also { intent ->
                                intent.putExtra("imageUri", compressedUri.toString())
                                requireActivity().setResult(Activity.RESULT_OK, intent)
                                requireActivity().finish()
                            }
                        }
                    } else {
                        uri.let {newUri->
                            Intent().also { intent ->
                                intent.putExtra("imageUri", newUri.toString())
                                requireActivity().setResult(Activity.RESULT_OK, intent)
                                requireActivity().finish()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun compressImageTo1MB(context: Context, imageUri: Uri): Uri? {
        val resolver: ContentResolver = context.contentResolver

        val inputBytes =
            resolver.openInputStream(imageUri)?.use { inputStream -> inputStream.readBytes() }
                ?: return null
        val originalBitmap = BitmapFactory.decodeByteArray(inputBytes, 0, inputBytes.size)

        val rotation = AppUtils.getRotation(requireContext(), imageUri)
        val rotatedBitmap = if (rotation > 0f) {
            AppUtils.rotateBitmap(originalBitmap, rotation)
        } else {
            originalBitmap
        }

        var quality = 100
        var outputBytes: ByteArray

        do {
            ByteArrayOutputStream().use { outputStream ->
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                outputBytes = outputStream.toByteArray()
                quality -= (quality * 0.1).roundToInt()
                AppUtils.loge("SIZE : ${outputBytes.size.div(1024 * 1024)}")
                AppUtils.loge("Quality : $quality")
            }
        } while (outputBytes.size > 1 * (1024 * 1024) && quality > 5)

        return saveCompressedImage(outputBytes)
    }

    private fun saveCompressedImage(compressedImageData: ByteArray): Uri? {
        val outputStream: OutputStream?
        val imageUri: Uri?

        val fileName = "IMG_Compressed_${System.currentTimeMillis()}.jpg"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = requireContext().contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/ImageCropper"
                )
            }
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            outputStream = imageUri?.let { resolver.openOutputStream(it) }
        } else {
            // For devices below Android Q
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + "/ImageCropper"
            val file = File(imagesDir)
            if (!file.exists()) {
                file.mkdirs()
            }
            val image = File(imagesDir, fileName)
            outputStream = FileOutputStream(image)
            imageUri = Uri.fromFile(image)
        }

        outputStream?.use {
            it.write(compressedImageData)
            it.flush()
        }

        return imageUri
    }
}