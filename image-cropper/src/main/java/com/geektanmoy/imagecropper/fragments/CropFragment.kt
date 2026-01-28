package com.geektanmoy.imagecropper.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.geektanmoy.imagecropper.databinding.FragmentCropBinding
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface


class CropFragment : Fragment() {

    private lateinit var binding: FragmentCropBinding
    private val args by navArgs<CropFragmentArgs>()
    private val imageUri: Uri? by lazy { args.imageUri.toUri() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCropBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        imageUri?.let {
            val imageBitmap =
                BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(imageUri!!))
            val rotatedBitmap = rotateBitmap(imageBitmap, getRotation(requireContext(), imageUri!!))
            //val rotatedBitmap = rotateBitmapIfRequired(imageBitmap, imageUri)
            //binding.ivImage.setImageURI(imageUri)
            binding.ivImage.setImageBitmapForCrop(rotatedBitmap)
        }

        binding.btnCrop.setOnClickListener {
            val cropImage = binding.ivImage.cropSelected()
            cropImage?.let { cropUri ->
                imageUri?.toString()?.let {
                    findNavController().navigate(
                        CropFragmentDirections.actionCropFragmentToCropViewFragment(
                            it,
                            cropUri.toString()
                        )
                    )
                }
            }
        }
    }

    private fun getRotation(context: Context, selectedImage: Uri): Float {
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

    private fun rotateBitmap(img: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }

    /*fun rotateBitmapIfRequired(bitmap: Bitmap, selectedImage: Uri): Bitmap {
        val exif = ExifInterface(requireContext().contentResolver.openInputStream(selectedImage)!!)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }*/
}