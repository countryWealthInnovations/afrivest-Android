package com.afrivest.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.afrivest.app.databinding.ActivityLoginBinding
import com.afrivest.app.ui.main.MainActivity
import com.afrivest.app.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun setupUI() {
        // Show/hide biometric button
        if (viewModel.isBiometricAvailable()) {
            binding.btnBiometric.visible()
        } else {
            binding.btnBiometric.gone()
        }
    }

    private fun setupListeners() {
        // Email
        binding.editTextEmail.doAfterTextChanged { text ->
            viewModel.onEmailChanged(text.toString())
        }

        // Password
        binding.editTextPassword.doAfterTextChanged { text ->
            viewModel.onPasswordChanged(text.toString())
        }

        // Login Button
        binding.btnLogin.setOnClickListener {
            viewModel.login()
        }

        // Forgot Password
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Register Link
        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Biometric Button
        binding.btnBiometric.setOnClickListener {
            showBiometricPrompt()
        }
    }

    private fun observeViewModel() {
        // Email State
        viewModel.emailState.observe(this) { state ->
            when (state) {
                is LoginViewModel.FieldState.Valid -> {
                    binding.textInputLayoutEmail.clearError()
                    binding.textInputLayoutEmail.showSuccess()
                }
                is LoginViewModel.FieldState.Invalid -> {
                    binding.textInputLayoutEmail.showError(state.message)
                }
                is LoginViewModel.FieldState.Normal -> {
                    binding.textInputLayoutEmail.clearError()
                }
            }
        }

        // Form Valid
        viewModel.isFormValid.observe(this) { isValid ->
            if (isValid) {
                binding.btnLogin.enable()
            } else {
                binding.btnLogin.disable()
            }
        }

        // Loading
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.loadingOverlay.root.visible()
                binding.btnLogin.disable()
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

        // Biometric Setup Prompt
        viewModel.shouldPromptBiometricSetup.observe(this) { shouldPrompt ->
            if (shouldPrompt) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Enable Biometric Login")
                    .setMessage("Would you like to use Face ID/Fingerprint to login faster next time?")
                    .setPositiveButton("Enable") { _, _ ->
                        viewModel.enableBiometric()
                    }
                    .setNegativeButton("Not Now") { _, _ ->
                        viewModel.skipBiometric()
                    }
                    .setCancelable(false)
                    .show()
            }
        }

        // Navigate to OTP
        viewModel.navigateToOTP.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, OTPActivity::class.java)
                intent.putExtra("email", viewModel.email.value)
                intent.putExtra("from", "login")
                startActivity(intent)
            }
        }

        // Show Email Verification Dialog
        viewModel.showEmailVerificationDialog.observe(this) { show ->
            if (show) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Email Verification Required")
                    .setMessage("Please verify your email to unlock all features. A verification code will be sent to your email.")
                    .setPositiveButton("Verify Now") { _, _ ->
                        viewModel.proceedToEmailVerification()
                    }
                    .setNegativeButton("Later") { _, _ ->
                        viewModel.skipEmailVerification()
                    }
                    .setCancelable(false)
                    .show()
            }
        }

        // Navigate to Dashboard
        viewModel.navigateToDashboard.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.loginWithBiometric()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    MaterialAlertDialogBuilder(this@LoginActivity)
                        .setTitle("Authentication Error")
                        .setMessage(errString)
                        .setPositiveButton("OK", null)
                        .show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Login to AfriVest")
            .setSubtitle("Use your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}