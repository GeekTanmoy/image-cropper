package com.geektanmoy.imagecropper

import android.util.Log

object AppUtils {

    var logFlag: Boolean = false

    fun loge(message: String, tag: String = "") {
        if (logFlag) {
            Log.e(tag.ifEmpty { "GeekTanmoy" }, message)
        }
    }
}