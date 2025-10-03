package com.afrivest.app.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class OTPBoxHandler(
    private val boxes: List<EditText>,
    private val onComplete: (String) -> Unit
) {

    init {
        boxes.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (!s.isNullOrEmpty()) {
                        // Move to next box
                        if (index < boxes.size - 1) {
                            boxes[index + 1].requestFocus()
                        } else {
                            // All boxes filled
                            val otp = boxes.joinToString("") { it.text.toString() }
                            if (otp.length == 6) {
                                onComplete(otp)
                            }
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            // Handle backspace
            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && editText.text.isEmpty()) {
                    if (index > 0) {
                        boxes[index - 1].requestFocus()
                        boxes[index - 1].setText("")
                    }
                    true
                } else {
                    false
                }
            }
        }
    }

    fun clear() {
        boxes.forEach { it.setText("") }
        boxes.first().requestFocus()
    }

    fun getOTP(): String {
        return boxes.joinToString("") { it.text.toString() }
    }
}

// Usage in Activity/Fragment:
/*
val otpBoxes = listOf(
    binding.otpBox1,
    binding.otpBox2,
    binding.otpBox3,
    binding.otpBox4,
    binding.otpBox5,
    binding.otpBox6
)

val otpHandler = OTPBoxHandler(otpBoxes) { otp ->
    // OTP complete, call API
    viewModel.verifyOTP(otp)
}
*/