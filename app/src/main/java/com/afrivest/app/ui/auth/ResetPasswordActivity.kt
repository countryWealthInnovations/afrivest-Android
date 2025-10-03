package com.afrivest.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.afrivest.app.R
import com.afrivest.app.databinding.ActivityResetPasswordBinding
import com.afrivest.app.databinding.ComponentPasswordStrengthBinding
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
class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private val viewModel: ResetPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = intent.getStringExtra("email") ?: ""
        viewModel.initialize(email)

        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun setupUI() {
        binding.tvEmail.text = viewModel.email.value
    }

    private fun setupListeners() {
        // Code
        binding.editTextCode.doAfterTextChanged { text ->
            viewModel.onCodeChanged(text.toString())
        }

        // Password
        binding.editTextPassword.doAfterTextChanged { text ->
            viewModel.onPasswordChanged(text.toString())
        }

        // Confirm Password
        binding.editTextConfirmPassword.doAfterTextChanged { text ->
            viewModel.onConfirmPasswordChanged(text.toString())
        }

        // Reset Password Button
        binding.btnResetPassword.setOnClickListener {
            viewModel.resetPassword()
        }
    }

    private fun observeViewModel() {
        // Code State
        viewModel.codeState.observe(this) { state ->
            when (state) {
                is ResetPasswordViewModel.FieldState.Valid -> {
                    binding.textInputLayoutCode.clearError()
                    binding.textInputLayoutCode.showSuccess()
                }
                is ResetPasswordViewModel.FieldState.Invalid -> {
                    binding.textInputLayoutCode.showError(state.message)
                }
                is ResetPasswordViewModel.FieldState.Normal -> {
                    binding.textInputLayoutCode.clearError()
                }
            }
        }

        // Password State
        viewModel.passwordState.observe(this) { state ->
            when (state) {
                is ResetPasswordViewModel.FieldState.Valid -> {
                    binding.textInputLayoutPassword.clearError()
                }
                is ResetPasswordViewModel.FieldState.Invalid -> {
                    binding.textInputLayoutPassword.showError(state.message)
                }
                is ResetPasswordViewModel.FieldState.Normal -> {
                    binding.textInputLayoutPassword.clearError()
                }
            }
        }

        // Password Strength
        viewModel.passwordStrength.observe(this) { strength ->
            binding.passwordStrength.root.visible()
            setPasswordStrength(binding.passwordStrength, strength)
        }

        // Confirm Password State
        viewModel.confirmPasswordState.observe(this) { state ->
            when (state) {
                is ResetPasswordViewModel.FieldState.Valid -> {
                    binding.textInputLayoutConfirmPassword.clearError()
                    binding.textInputLayoutConfirmPassword.showSuccess()
                }
                is ResetPasswordViewModel.FieldState.Invalid -> {
                    binding.textInputLayoutConfirmPassword.showError(state.message)
                }
                is ResetPasswordViewModel.FieldState.Normal -> {
                    binding.textInputLayoutConfirmPassword.clearError()
                }
            }
        }

        // Form Valid
        viewModel.isFormValid.observe(this) { isValid ->
            if (isValid) {
                binding.btnResetPassword.enable()
            } else {
                binding.btnResetPassword.disable()
            }
        }

        // Loading
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.loadingOverlay.root.visible()
                binding.btnResetPassword.disable()
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

        // Navigate to Login
        viewModel.navigateToLogin.observe(this) { navigate ->
            if (navigate) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Success")
                    .setMessage("Password reset successful. Please login with your new password.")
                    .setPositiveButton("OK") { _, _ ->
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                    .setCancelable(false)
                    .show()
            }
        }
    }

    private fun setPasswordStrength(
        binding: ComponentPasswordStrengthBinding,
        strength: ResetPasswordViewModel.PasswordStrength
    ) {
        when (strength) {
            ResetPasswordViewModel.PasswordStrength.WEAK -> {
                binding.strengthBar1.setBackgroundColor(getColor(R.color.error_red))
                binding.strengthBar2.setBackgroundColor(getColor(R.color.border_default))
                binding.strengthBar3.setBackgroundColor(getColor(R.color.border_default))
                binding.tvStrengthLabel.text = "Weak password"
                binding.tvStrengthLabel.setTextColor(getColor(R.color.error_red))
            }
            ResetPasswordViewModel.PasswordStrength.MEDIUM -> {
                binding.strengthBar1.setBackgroundColor(getColor(R.color.warning_yellow))
                binding.strengthBar2.setBackgroundColor(getColor(R.color.warning_yellow))
                binding.strengthBar3.setBackgroundColor(getColor(R.color.border_default))
                binding.tvStrengthLabel.text = "Medium password"
                binding.tvStrengthLabel.setTextColor(getColor(R.color.warning_yellow))
            }
            ResetPasswordViewModel.PasswordStrength.STRONG -> {
                binding.strengthBar1.setBackgroundColor(getColor(R.color.success_green))
                binding.strengthBar2.setBackgroundColor(getColor(R.color.success_green))
                binding.strengthBar3.setBackgroundColor(getColor(R.color.success_green))
                binding.tvStrengthLabel.text = "Strong password"
                binding.tvStrengthLabel.setTextColor(getColor(R.color.success_green))
            }
        }
    }
}