package com.afrivest.app.utils

import android.content.res.ColorStateList
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.afrivest.app.R
import com.google.android.material.textfield.TextInputLayout

// Show/Hide View
fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

// TextInputLayout Extensions
fun TextInputLayout.showError(message: String) {
    error = message
    isErrorEnabled = true
}

fun TextInputLayout.clearError() {
    error = null
    isErrorEnabled = false
}

fun TextInputLayout.showSuccess() {
    setEndIconDrawable(R.drawable.ic_checkmark_circle)
    setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.success_green)))
}

// EditText validation
fun EditText.onTextChanged(action: (String) -> Unit) {
    doAfterTextChanged { editable ->
        action(editable?.toString() ?: "")
    }
}

// Enable/Disable button
fun View.enable() {
    isEnabled = true
    alpha = 1f
}

fun View.disable() {
    isEnabled = false
    alpha = 0.5f
}