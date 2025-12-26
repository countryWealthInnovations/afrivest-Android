package com.afrivest.app.ui.profile

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.afrivest.app.R
import com.afrivest.app.databinding.ActivityChangePasswordBinding
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
class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private val viewModel: ChangePasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        // Current Password
        binding.editTextCurrentPassword.doAfterTextChanged { text ->
            viewModel.onCurrentPasswordChanged(text.toString())
        }

        // New Password
        binding.editTextNewPassword.doAfterTextChanged { text ->
            viewModel.onNewPasswordChanged(text.toString())
        }

        // Confirm Password
        binding.editTextConfirmPassword.doAfterTextChanged { text ->
            viewModel.onConfirmPasswordChanged(text.toString())
        }

        // Change Password Button
        binding.btnChangePassword.setOnClickListener {
            viewModel.changePassword()
        }
    }

    private fun observeViewModel() {
        // Current Password State
        viewModel.currentPasswordState.observe(this) { state ->
            when (state) {
                is ChangePasswordViewModel.FieldState.Valid -> {
                    binding.textInputLayoutCurrentPassword.clearError()
                    binding.textInputLayoutCurrentPassword.showSuccess()
                }
                is ChangePasswordViewModel.FieldState.Invalid -> {
                    binding.textInputLayoutCurrentPassword.showError(state.message)
                }
                is ChangePasswordViewModel.FieldState.Normal -> {
                    binding.textInputLayoutCurrentPassword.clearError()
                }
            }
        }

        // New Password State
        viewModel.newPasswordState.observe(this) { state ->
            when (state) {
                is ChangePasswordViewModel.FieldState.Valid -> {
                    binding.textInputLayoutNewPassword.clearError()
                }
                is ChangePasswordViewModel.FieldState.Invalid -> {
                    binding.textInputLayoutNewPassword.showError(state.message)
                }
                is ChangePasswordViewModel.FieldState.Normal -> {
                    binding.textInputLayoutNewPassword.clearError()
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
                is ChangePasswordViewModel.FieldState.Valid -> {
                    binding.textInputLayoutConfirmPassword.clearError()
                    binding.textInputLayoutConfirmPassword.showSuccess()
                }
                is ChangePasswordViewModel.FieldState.Invalid -> {
                    binding.textInputLayoutConfirmPassword.showError(state.message)
                }
                is ChangePasswordViewModel.FieldState.Normal -> {
                    binding.textInputLayoutConfirmPassword.clearError()
                }
            }
        }

        // Form Valid
        viewModel.isFormValid.observe(this) { isValid ->
            if (isValid) {
                binding.btnChangePassword.enable()
            } else {
                binding.btnChangePassword.disable()
            }
        }

        // Loading
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.loadingOverlay.root.visible()
                binding.btnChangePassword.disable()
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

        // Success
        viewModel.changePasswordSuccess.observe(this) { success ->
            if (success) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Success")
                    .setMessage("Password changed successfully!")
                    .setPositiveButton("OK") { _, _ ->
                        finish()
                    }
                    .setCancelable(false)
                    .show()
            }
        }
    }

    private fun setPasswordStrength(
        binding: ComponentPasswordStrengthBinding,
        strength: ChangePasswordViewModel.PasswordStrength
    ) {
        when (strength) {
            ChangePasswordViewModel.PasswordStrength.WEAK -> {
                binding.strengthBar1.setBackgroundColor(getColor(R.color.error_red))
                binding.strengthBar2.setBackgroundColor(getColor(R.color.border_default))
                binding.strengthBar3.setBackgroundColor(getColor(R.color.border_default))
                binding.tvStrengthLabel.text = "Weak password"
                binding.tvStrengthLabel.setTextColor(getColor(R.color.error_red))
            }
            ChangePasswordViewModel.PasswordStrength.MEDIUM -> {
                binding.strengthBar1.setBackgroundColor(getColor(R.color.warning_yellow))
                binding.strengthBar2.setBackgroundColor(getColor(R.color.warning_yellow))
                binding.strengthBar3.setBackgroundColor(getColor(R.color.border_default))
                binding.tvStrengthLabel.text = "Medium password"
                binding.tvStrengthLabel.setTextColor(getColor(R.color.warning_yellow))
            }
            ChangePasswordViewModel.PasswordStrength.STRONG -> {
                binding.strengthBar1.setBackgroundColor(getColor(R.color.success_green))
                binding.strengthBar2.setBackgroundColor(getColor(R.color.success_green))
                binding.strengthBar3.setBackgroundColor(getColor(R.color.success_green))
                binding.tvStrengthLabel.text = "Strong password"
                binding.tvStrengthLabel.setTextColor(getColor(R.color.success_green))
            }
        }
    }
}