package com.geektanmoy.imagecropper.utils

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object AppUtils {

    var logFlag: Boolean = false

    fun loge(message: String, tag: String = "") {
        if (logFlag) {
            Log.e(tag.ifEmpty { "GeekTanmoy" }, message)
        }
    }

     fun fileFromContentUri(context: Context, contentUri: Uri): File {

        //val fileExtension = getFileExtension(context, contentUri)
        /*val fileName = getFileName(contentUri)
            ?: ("Pan" + if (fileExtension != null) ".$fileExtension" else "")*/
        //val fileName = "Pan Card" + if (fileExtension != null) ".$fileExtension" else ""
        val fileName = getFileName(context, contentUri)

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
    private fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? =
                context.contentResolver.query(uri, null, null, null, null)
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

    fun getMimeType(extension: String): String? {
        loge("Extension : $extension")
        var type: String? = null
        if (extension.isNotEmpty()) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    fun getRotation(context: Context, selectedImage: Uri): Float {
        val ei = ExifInterface(context.contentResolver.openInputStream(selectedImage)!!)
        val orientation: Int =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_NORMAL -> 0f
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            ExifInterface.ORIENTATION_UNDEFINED -> 0f
            else -> 90f
        }
    }

    fun rotateBitmap(img: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        //img.recycle()
        return rotatedImg
    }
}