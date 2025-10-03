package com.afrivest.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.afrivest.app.databinding.ActivityOtpBinding
import com.afrivest.app.ui.main.MainActivity
import com.afrivest.app.utils.OTPBoxHandler
import com.afrivest.app.utils.gone
import com.afrivest.app.utils.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OTPActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpBinding
    private val viewModel: OTPViewModel by viewModels()
    private lateinit var otpHandler: OTPBoxHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = intent.getStringExtra("email") ?: ""
        val from = intent.getStringExtra("from") ?: ""

        viewModel.initialize(email, from)

        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun setupUI() {
        // Initialize OTP boxes
        val otpBoxes = listOf(
            binding.otpBox1,
            binding.otpBox2,
            binding.otpBox3,
            binding.otpBox4,
            binding.otpBox5,
            binding.otpBox6
        )

        otpHandler = OTPBoxHandler(otpBoxes) { otp ->
            viewModel.onOTPChanged(otp)
        }
    }

    private fun setupListeners() {
        // Verify Button
        binding.btnVerify.setOnClickListener {
            viewModel.verifyOTP()
        }

        // Resend Button
        binding.tvResend.setOnClickListener {
            viewModel.resendOTP()
        }
    }

    private fun observeViewModel() {
        // Email
        viewModel.email.observe(this) { email ->
            binding.tvEmail.text = email
        }

        // Timer
        viewModel.timeRemaining.observe(this) { time ->
            binding.tvTimer.text = "Code expires in ${viewModel.getFormattedTime()}"

            // Change color if less than 1 minute
            if (time < 60) {
                binding.tvTimer.setTextColor(getColor(com.afrivest.app.R.color.error_red))
            } else {
                binding.tvTimer.setTextColor(getColor(com.afrivest.app.R.color.text_secondary))
            }
        }

        // Can Resend
        viewModel.canResend.observe(this) { canResend ->
            binding.tvResend.isEnabled = canResend
            if (canResend) {
                binding.tvResend.setTextColor(getColor(com.afrivest.app.R.color.primary_gold))
            } else {
                binding.tvResend.setTextColor(getColor(com.afrivest.app.R.color.text_disabled))
            }
        }

        // OTP Code
        viewModel.otpCode.observe(this) { code ->
            binding.btnVerify.isEnabled = code.length == 6
        }

        // Loading
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.loadingOverlay.root.visible()
                binding.btnVerify.isEnabled = false
            } else {
                binding.loadingOverlay.root.gone()
            }
        }

        // Error
        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Error")
                    .setMessage(it)
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        // Navigate to Dashboard
        viewModel.navigateToDashboard.observe(this) { navigate ->
            if (navigate) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}