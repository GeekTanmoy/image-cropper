package com.geektanmoy.imagecropper.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.geektanmoy.imagecropper.AppUtils
import com.geektanmoy.imagecropper.databinding.FragmentCropViewBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.roundToInt


class CropViewFragment : Fragment() {

    private lateinit var binding: FragmentCropViewBinding
    private val args by navArgs<CropViewFragmentArgs>()
    private val cropUri: Uri? by lazy { args.cropUri.toUri() }
    private val imageUri: Uri? by lazy { args.imageUri.toUri() }
    private var compressedUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCropViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        //cropUri?.let { binding.ivImage.setImageURI(it) }


        if (cropUri != null) {
            val cropImageFile: File = fileFromContentUri(requireContext(), cropUri!!)
            AppUtils.loge( "Crop Image File : $cropImageFile")
            AppUtils.loge("Crop Image File Mime Type : ${getMimeType(cropImageFile.extension)}")
            AppUtils.loge("Crop Image File Size : ${cropImageFile.length()}Bytes")
            AppUtils.loge( "Crop Image File Size : ${cropImageFile.length().div(1024)}KB")
            AppUtils.loge( "Crop Image File Size : ${cropImageFile.length().div((1024 * 1024))}MB")

            if (cropImageFile.length() > (1024 * 1024)) { //Less than 1MB
                compressedUri = compressImageTo1MB(requireContext(), cropUri!!)
                compressedUri?.let { binding.ivImage.setImageURI(it) }

                val compressedFile: File = fileFromContentUri(requireContext(), compressedUri!!)
                AppUtils.loge( "Compressed Image File : $compressedFile")
                AppUtils.loge( "Compressed File Mime Type : ${getMimeType(compressedFile.extension)}")
                AppUtils.loge( "Compressed File Size : ${compressedFile.length()}Bytes")
                AppUtils.loge( "Compressed File Size : ${compressedFile.length().div(1024)}KB")
                AppUtils.loge(
                    "Compressed File Size : ${compressedFile.length().div((1024 * 1024))}MB"
                )
            }
        }

        if (imageUri != null) {
            val imageFile: File = fileFromContentUri(requireContext(), imageUri!!)
            AppUtils.loge( "Image File : $imageFile")
            AppUtils.loge( "Image File Mime Type : ${getMimeType(imageFile.extension)}")
            AppUtils.loge( "Image File Size : ${imageFile.length()}Bytes")
            AppUtils.loge( "Image File Size : ${imageFile.length().div(1024)}KB")
            AppUtils.loge( "Image File Size : ${imageFile.length().div((1024 * 1024))}MB")
        }

        binding.btnUse.setOnClickListener {
            compressedUri?.let {
                Intent().also { intent ->
                    intent.putExtra("imageUri", compressedUri?.toString())
                    requireActivity().setResult(Activity.RESULT_OK, intent)
                    requireActivity().finish()
                }
            }
        }
    }

    private fun fileFromContentUri(context: Context, contentUri: Uri): File {

        //val fileExtension = getFileExtension(context, contentUri)
        /*val fileName = getFileName(contentUri)
            ?: ("Pan" + if (fileExtension != null) ".$fileExtension" else "")*/
        //val fileName = "Pan Card" + if (fileExtension != null) ".$fileExtension" else ""
        val fileName = getFileName(contentUri)

        val tempFile = File(context.cacheDir, fileName)
        tempFile.createNewFile()

        try {
            val oStream = FileOutputStream(tempFile)
            val inputStream = context.contentResolver.openInputStream(contentUri)

            inputStream?.let {
                copy(inputStream, oStream)
            }
            oStream.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return tempFile
    }

    @SuppressLint("Range")
    fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? =
                requireContext().contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    @Throws(IOException::class)
    private fun copy(source: InputStream, target: OutputStream) {
        val buf = ByteArray(8192)
        var length: Int
        while (source.read(buf).also { length = it } > 0) {
            target.write(buf, 0, length)
        }
    }

    private fun getMimeType(extension: String): String? {
        AppUtils.loge( "Extension : $extension")
        var type: String? = null
        if (extension.isNotEmpty()) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    private fun compressImageTo1MB(context: Context, imageUri: Uri): Uri? {
        val resolver: ContentResolver = context.contentResolver

        // Decode image
        /*val inputStream = resolver.openInputStream(imageUri)?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()*/

        val inputBytes =
            resolver.openInputStream(imageUri)?.use { inputStream -> inputStream.readBytes() }
                ?: return null
        val originalBitmap = BitmapFactory.decodeByteArray(inputBytes, 0, inputBytes.size)

        var quality = 100
        //val outputStream = ByteArrayOutputStream()
        var outputBytes: ByteArray

        // Compress until <= 1MB
        /*do {
            outputStream.reset()
            originalBitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream)
            quality -= 5
        } while (outputStream.size() > 1_000_000 && quality > 5)*/

        do {
            ByteArrayOutputStream().use { outputStream ->
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                outputBytes = outputStream.toByteArray()
                quality -= (quality * 0.1).roundToInt()
                AppUtils.loge( "SIZE : ${outputBytes.size.div(1024 * 1024)}")
                AppUtils.loge( "Quality : $quality")
            }
        } while (outputBytes.size > 1 * (1024 * 1024) && quality > 5)

        /*// Save compressed file
        val compressedFile = File(context.cacheDir, "Compressed_${System.currentTimeMillis()}.png")
        val fos = FileOutputStream(compressedFile)
        fos.write(outputBytes)
        fos.flush()
        fos.close()*/

        //return Uri.fromFile(compressedFile)
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