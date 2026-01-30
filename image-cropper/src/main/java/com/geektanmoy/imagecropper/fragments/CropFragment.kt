package com.geektanmoy.imagecropper.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.geektanmoy.imagecropper.utils.AppUtils
import com.geektanmoy.imagecropper.databinding.FragmentCropBinding
import com.geektanmoy.imagecropper.utils.onBackButtonPressed


class CropFragment : Fragment() {

    private lateinit var binding: FragmentCropBinding
    private val args by navArgs<CropFragmentArgs>()
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCropBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AppUtils.loge("Image Uri : ${args.imageUri}")
        AppUtils.loge("Action : ${args.action}")


        binding.ivBack.setOnClickListener {
            imageUri?.let { uri ->
                findNavController().navigate(
                    CropFragmentDirections.actionCropFragmentToPreviewFragment(
                        uri.toString(),
                        args.action,
                        false,
                        null
                    )
                )
            }
        }

        onBackButtonPressed {
            binding.ivBack.performClick()
            true
        }

        args.imageUri.let { uri ->
            imageUri = uri.toUri()
            val imageBitmap: Bitmap = BitmapFactory.decodeStream(
                requireContext().contentResolver.openInputStream(
                    uri.toUri()
                )
            )

            val rotation = AppUtils.getRotation(requireContext(), uri.toUri())
            if (args.action == "G") {
                if (rotation > 0f) {
                    val rotatedBitmap = AppUtils.rotateBitmap(imageBitmap, rotation)
                    binding.ivImage.setImageBitmapForCrop(rotatedBitmap)
                } else {
                    binding.ivImage.setImageBitmapForCrop(imageBitmap)
                }
            } else if (args.action == "C") {
                val rotatedBitmap =
                    AppUtils.rotateBitmap(imageBitmap, rotation)
                binding.ivImage.setImageBitmapForCrop(rotatedBitmap)
            }
        }

        binding.btnCrop.setOnClickListener {
            val cropImage = binding.ivImage.cropSelected()
            cropImage?.let { cropUri ->
                findNavController().navigate(
                    CropFragmentDirections.actionCropFragmentToPreviewFragment(
                        imageUri.toString(),
                        args.action,
                        true,
                        cropUri.toString()

                    )
                )
            }
        }
    }
}