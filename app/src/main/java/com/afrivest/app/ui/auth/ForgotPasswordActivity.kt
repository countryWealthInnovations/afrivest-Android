package com.afrivest.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.afrivest.app.databinding.ActivityForgotPasswordBinding
import com.afrivest.app.utils.clearError
import com.afrivest.app.utils.disable
import com.afrivest.app.utils.enable
import com.afrivest.app.utils.gone
import com.afrivest.app.utils.showError
import com.afrivest.app.utils.showSuccess
import com.afrivest.app.utils.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Email
        binding.editTextEmail.doAfterTextChanged { text ->
            viewModel.onEmailChanged(text.toString())
        }

        // Send Code Button
        binding.btnSendCode.setOnClickListener {
            viewModel.sendResetCode()
        }
    }

    private fun observeViewModel() {
        // Email State
        viewModel.emailState.observe(this) { state ->
            when (state) {
                is ForgotPasswordViewModel.FieldState.Valid -> {
                    binding.textInputLayoutEmail.clearError()
                    binding.textInputLayoutEmail.showSuccess()
                }
                is ForgotPasswordViewModel.FieldState.Invalid -> {
                    binding.textInputLayoutEmail.showError(state.message)
                }
                is ForgotPasswordViewModel.FieldState.Normal -> {
                    binding.textInputLayoutEmail.clearError()
                }
            }
        }

        // Form Valid
        viewModel.isFormValid.observe(this) { isValid ->
            if (isValid) {
                binding.btnSendCode.enable()
            } else {
                binding.btnSendCode.disable()
            }
        }

        // Loading
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.loadingOverlay.root.visible()
                binding.btnSendCode.disable()
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

        // Navigate to Reset Password
        viewModel.navigateToResetPassword.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, ResetPasswordActivity::class.java)
                intent.putExtra("email", viewModel.email.value)
                startActivity(intent)
                finish()
            }
        }
    }
}