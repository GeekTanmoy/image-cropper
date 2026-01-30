package com.geektanmoy.imagecropper

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt


class DraggableCropImageView(context: Context, attrs: AttributeSet?) :
    AppCompatImageView(context, attrs) {

    private var bitmap: Bitmap? = null

    private val paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    private val handleRadius = 20f
    private val handlePaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL_AND_STROKE
    }

    private var rect = RectF(100f, 100f, 400f, 400f)

    private enum class Mode { NONE, MOVE, RESIZE_TOP_LEFT, RESIZE_TOP_RIGHT, RESIZE_BOTTOM_LEFT, RESIZE_BOTTOM_RIGHT }

    private var mode = Mode.NONE

    private var lastX = 0f
    private var lastY = 0f

    fun setImageBitmapForCrop(bmp: Bitmap) {
            bitmap = bmp
            setImageBitmap(bitmap)

            rect = RectF(100f, 100f, 400f, 400f)
            invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(rect, paint)

        // Draw corner handles
        canvas.drawCircle(rect.left, rect.top, handleRadius, handlePaint)
        canvas.drawCircle(rect.right, rect.top, handleRadius, handlePaint)
        canvas.drawCircle(rect.left, rect.bottom, handleRadius, handlePaint)
        canvas.drawCircle(rect.right, rect.bottom, handleRadius, handlePaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mode = getTouchMode(x, y)
                lastX = x
                lastY = y
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = x - lastX
                val dy = y - lastY
                when (mode) {
                    Mode.MOVE -> {
                        rect.offset(dx, dy)
                        constrainRect()
                    }

                    Mode.RESIZE_TOP_LEFT -> {
                        rect.left += dx
                        rect.top += dy
                        constrainRect()
                    }

                    Mode.RESIZE_TOP_RIGHT -> {
                        rect.right += dx
                        rect.top += dy
                        constrainRect()
                    }

                    Mode.RESIZE_BOTTOM_LEFT -> {
                        rect.left += dx
                        rect.bottom += dy
                        constrainRect()
                    }

                    Mode.RESIZE_BOTTOM_RIGHT -> {
                        rect.right += dx
                        rect.bottom += dy
                        constrainRect()
                    }

                    else -> {}
                }
                lastX = x
                lastY = y
                invalidate()
            }

            MotionEvent.ACTION_UP -> mode = Mode.NONE
        }
        return true
    }

    private fun getTouchMode(x: Float, y: Float): Mode {
        if (distance(x, y, rect.left, rect.top) <= handleRadius) return Mode.RESIZE_TOP_LEFT
        if (distance(x, y, rect.right, rect.top) <= handleRadius) return Mode.RESIZE_TOP_RIGHT
        if (distance(x, y, rect.left, rect.bottom) <= handleRadius) return Mode.RESIZE_BOTTOM_LEFT
        if (distance(x, y, rect.right, rect.bottom) <= handleRadius) return Mode.RESIZE_BOTTOM_RIGHT
        if (rect.contains(x, y)) return Mode.MOVE
        return Mode.NONE
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Double {
        return sqrt((x1 - x2).pow(2) + (y1 - y2).pow(2))
    }

    private fun Float.pow(n: Int) = this.toDouble().pow(n)

    private fun constrainRect() {
        bitmap?.let {
            if (rect.left < 0) rect.offset(-rect.left, 0f)
            if (rect.top < 0) rect.offset(0f, -rect.top)
            if (rect.right > width) rect.offset(width - rect.right, 0f)
            if (rect.bottom > height) rect.offset(0f, height - rect.bottom)
        }
    }

    fun cropSelected(): Uri? {
        bitmap?.let { bmp ->
            val imageMatrix = imageMatrix
            val values = FloatArray(9)
            imageMatrix.getValues(values)

            val scaleX = values[Matrix.MSCALE_X]
            val scaleY = values[Matrix.MSCALE_Y]
            val transX = values[Matrix.MTRANS_X]
            val transY = values[Matrix.MTRANS_Y]

            val left = ((rect.left - transX) / scaleX).toInt().coerceIn(0, bmp.width)
            val top = ((rect.top - transY) / scaleY).toInt().coerceIn(0, bmp.height)
            val right = ((rect.right - transX) / scaleX).toInt().coerceIn(0, bmp.width)
            val bottom = ((rect.bottom - transY) / scaleY).toInt().coerceIn(0, bmp.height)

            val width = right - left
            val height = bottom - top

            if (width <= 0 || height <= 0) return null

            val createBitmap = Bitmap.createBitmap(bmp, left, top, width, height)

            return getImageUri(createBitmap)
        }
        return null
    }


    private fun getImageUri(bitmap: Bitmap): Uri? {
        var uri: Uri? = null
        try {
            val timestamp =
                SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault()).format(Date())
            //val fileName = System.nanoTime().toString() + ".png"
            val fileName = "Crop_IMG_${timestamp}" + ".jpg"

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                } else {
                    val directory =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    val file = File(directory, fileName)
                    put(MediaStore.MediaColumns.DATA, file.absolutePath)
                }
            }

            uri =
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                context.contentResolver.openOutputStream(it).use { output ->
                    if (output != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.apply {
                        clear()
                        put(MediaStore.Audio.Media.IS_PENDING, 0)
                    }
                    context.contentResolver.update(uri, values, null, null)
                }
            }
            return uri
        } catch (e: Exception) {
            if (uri != null) {
                context.contentResolver.delete(uri, null, null)
            }
            throw e
        }
    }
}
