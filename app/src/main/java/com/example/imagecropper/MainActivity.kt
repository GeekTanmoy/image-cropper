package com.example.imagecropper

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.imagecropper.databinding.ActivityMainBinding
import com.example.imagecropper.databinding.LayoutCameraGalleryDialogBinding
import com.geektanmoy.imagecropper.utils.AppUtils
import com.geektanmoy.imagecropper.CropActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnOpen.setOnClickListener {
            showCameraGalleryDialog()
        }
    }

    @SuppressLint("SetTextI18n")
    fun showCameraGalleryDialog() {
        val cameraGalleryDialog =
            BottomSheetDialog(this)
        val cameraGalleryBinding =
            LayoutCameraGalleryDialogBinding.inflate(LayoutInflater.from(this))
        cameraGalleryDialog.setCancelable(true)
        cameraGalleryDialog.setCanceledOnTouchOutside(true)
        cameraGalleryDialog.setContentView(cameraGalleryBinding.root)

        cameraGalleryBinding.ivCamera.setOnClickListener {
            cameraGalleryDialog.dismiss()

            //Camera
            cameraLauncher.launch(Intent(this, CropActivity::class.java).also { intent ->
                intent.putExtra("ACTION", "C")
            })
        }

        cameraGalleryBinding.ivGallery.setOnClickListener {
            cameraGalleryDialog.dismiss()

            //Gallery
            galleryLauncher.launch(Intent(this, CropActivity::class.java).also { intent ->
                intent.putExtra("ACTION", "G")
                intent.putExtra("MIME", "IP")
            })
        }

        cameraGalleryDialog.show()
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri = result.data?.getStringExtra("imageUri")
                if (imageUri != null) {
                    binding.ivImage.setImageURI(imageUri.toUri())
                }
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri = result.data?.getStringExtra("imageUri")
                if (imageUri != null) {
                    val file: File = AppUtils.fileFromContentUri(this, imageUri.toUri())
                    AppUtils.loge("File : $file")
                    AppUtils.loge("File Mime Type : ${AppUtils.getMimeType(file.extension)}")
                    AppUtils.loge("File Size : ${file.length()}Bytes")
                    AppUtils.loge("File Size : ${file.length().div(1024)}KB")
                    AppUtils.loge("File Size : ${file.length().div((1024 * 1024))}MB")

                    if (file.extension.lowercase() == "pdf") {
                        binding.tvTitle.text = file.name
                    } else {
                        binding.tvTitle.text = file.name
                        binding.ivImage.setImageURI(imageUri.toUri())
                    }
                }
            }
        }
}