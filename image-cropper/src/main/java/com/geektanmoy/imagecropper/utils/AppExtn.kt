package com.geektanmoy.imagecropper.utils

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

fun Fragment.onBackButtonPressed(callback: (() -> Boolean)) {
    requireActivity().onBackPressedDispatcher.addCallback(
        viewLifecycleOwner,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!callback()) {
                    remove()
                    performBackPress()
                }
            }
        })
}

fun Fragment.performBackPress() {
    requireActivity().onBackPressedDispatcher.onBackPressed()
}