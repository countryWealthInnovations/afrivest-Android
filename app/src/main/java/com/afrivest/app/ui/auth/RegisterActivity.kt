package com.afrivest.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.afrivest.app.R
import com.afrivest.app.ui.auth.models.Country
import com.afrivest.app.databinding.ActivityRegisterBinding
import com.afrivest.app.ui.components.CountryPickerDialog
import com.afrivest.app.utils.clearError
import com.afrivest.app.utils.disable
import com.afrivest.app.utils.enable
import com.afrivest.app.utils.showError
import com.afrivest.app.utils.showSuccess
import com.afrivest.app.utils.visible
import com.afrivest.app.utils.gone
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import com.afrivest.app.databinding.ComponentPasswordStrengthBinding

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun setupUI() {
        // Set default country
        viewModel.selectedCountry.value?.let { updateCountryCode(it) }
    }

    private fun setupListeners() {
        // Country Code Selector
        binding.btnCountryCode.setOnClickListener {
            showCountryPicker()
        }

        // Full Name
        binding.editTextName.doAfterTextChanged { text ->
            viewModel.onNameChanged(text.toString())
        }

        // Email
        binding.editTextEmail.doAfterTextChanged { text ->
            viewModel.onEmailChanged(text.toString())
        }

        // Phone
        binding.editTextPhone.doAfterTextChanged { text ->
            viewModel.onPhoneChanged(text.toString())
        }

        // Password
        binding.editTextPassword.doAfterTextChanged { text ->
            viewModel.onPasswordChanged(text.toString())
        }

        // Confirm Password
        binding.editTextConfirmPassword.doAfterTextChanged { text ->
            viewModel.onConfirmPasswordChanged(text.toString())
        }

        // Terms Checkbox
        binding.checkboxTerms.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onTermsAccepted(isChecked)
        }

        // Terms & Conditions Text
        binding.tvTermsAndConditions.apply {
            val fullText = "I agree to the Terms & Conditions and Privacy Policy"
            val spannableString = android.text.SpannableString(fullText)

            val termsStart = fullText.indexOf("Terms & Conditions")
            val termsEnd = termsStart + "Terms & Conditions".length
            val privacyStart = fullText.indexOf("Privacy Policy")
            val privacyEnd = privacyStart + "Privacy Policy".length

            val termsClick = object : android.text.style.ClickableSpan() {
                override fun onClick(widget: android.view.View) {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                    intent.data = android.net.Uri.parse("https://afrivest.co/terms")
                    startActivity(intent)
                }
                override fun updateDrawState(ds: android.text.TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = getColor(R.color.primary_gold)
                    ds.isUnderlineText = true
                }
            }

            val privacyClick = object : android.text.style.ClickableSpan() {
                override fun onClick(widget: android.view.View) {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                    intent.data = android.net.Uri.parse("https://afrivest.co/privacy")
                    startActivity(intent)
                }
                override fun updateDrawState(ds: android.text.TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = getColor(R.color.primary_gold)
                    ds.isUnderlineText = true
                }
            }

            spannableString.setSpan(termsClick, termsStart, termsEnd, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(privacyClick, privacyStart, privacyEnd, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            text = spannableString
            movementMethod = android.text.method.LinkMovementMethod.getInstance()
        }

        // Register Button
        binding.btnRegister.setOnClickListener {
            viewModel.register()
        }

        // Login Link
        binding.tvLoginLink.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        // Name State
        viewModel.nameState.observe(this) { state ->
            when (state) {
                is RegisterViewModel.FieldState.Valid -> {
                    binding.textInputLayoutName.clearError()
                    binding.textInputLayoutName.showSuccess()
                }
                is RegisterViewModel.FieldState.Invalid -> {
                    binding.textInputLayoutName.showError(state.message)
                }
                is RegisterViewModel.FieldState.Normal -> {
                    binding.textInputLayoutName.clearError()
                }
            }
        }

        // Email State
        viewModel.emailState.observe(this) { state ->
            when (state) {
                is RegisterViewModel.FieldState.Valid -> {
                    binding.textInputLayoutEmail.clearError()
                    binding.textInputLayoutEmail.showSuccess()
                }
                is RegisterViewModel.FieldState.Invalid -> {
                    binding.textInputLayoutEmail.showError(state.message)
                }
                is RegisterViewModel.FieldState.Normal -> {
                    binding.textInputLayoutEmail.clearError()
                }
            }
        }

        // Phone State
        // Phone State
        viewModel.phoneState.observe(this) { state ->
            when (state) {
                is RegisterViewModel.FieldState.Valid -> {
                    binding.tvPhoneHelper.text = "Valid phone number"
                    binding.tvPhoneHelper.setTextColor(getColor(R.color.success_green))
                }
                is RegisterViewModel.FieldState.Invalid -> {
                    binding.tvPhoneHelper.text = state.message
                    binding.tvPhoneHelper.setTextColor(getColor(R.color.error_red))
                }
                is RegisterViewModel.FieldState.Normal -> {
                    binding.tvPhoneHelper.text = "Enter your phone number without country code"
                    binding.tvPhoneHelper.setTextColor(getColor(R.color.text_secondary))
                }
            }
        }

        // Password State
        viewModel.passwordState.observe(this) { state ->
            when (state) {
                is RegisterViewModel.FieldState.Valid -> {
                    binding.textInputLayoutPassword.clearError()
                }
                is RegisterViewModel.FieldState.Invalid -> {
                    binding.textInputLayoutPassword.showError(state.message)
                }
                is RegisterViewModel.FieldState.Normal -> {
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
                is RegisterViewModel.FieldState.Valid -> {
                    binding.textInputLayoutConfirmPassword.clearError()
                    binding.textInputLayoutConfirmPassword.showSuccess()
                }
                is RegisterViewModel.FieldState.Invalid -> {
                    binding.textInputLayoutConfirmPassword.showError(state.message)
                }
                is RegisterViewModel.FieldState.Normal -> {
                    binding.textInputLayoutConfirmPassword.clearError()
                }
            }
        }

        // Form Valid
        viewModel.isFormValid.observe(this) { isValid ->
            if (isValid) {
                binding.btnRegister.enable()
            } else {
                binding.btnRegister.disable()
            }
        }

        // Loading
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.loadingOverlay.root.visible()
                binding.btnRegister.disable()
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

        // Selected Country
        viewModel.selectedCountry.observe(this) { country ->
            updateCountryCode(country)
        }

        // Navigate to OTP
        viewModel.navigateToOTP.observe(this) { navigate ->
            if (navigate) {
                val intent = Intent(this, OTPActivity::class.java)
                intent.putExtra("email", viewModel.email.value)
                intent.putExtra("from", "register")
                startActivity(intent)
            }
        }
    }

    private fun updateCountryCode(country: Country) {
        binding.tvCountryFlag.text = country.flag
        binding.tvCountryCode.text = country.dialCode
    }

    private fun showCountryPicker() {
        val dialog = CountryPickerDialog()
        dialog.setOnCountrySelectedListener { country ->
            viewModel.onCountrySelected(country)
        }
        dialog.show(supportFragmentManager, "CountryPicker")
    }

    private fun setPasswordStrength(
        binding: ComponentPasswordStrengthBinding,
        strength: RegisterViewModel.PasswordStrength
    ) {
        when (strength) {
            RegisterViewModel.PasswordStrength.WEAK -> {
                binding.strengthBar1.setBackgroundColor(getColor(R.color.error_red))
                binding.strengthBar2.setBackgroundColor(getColor(R.color.border_default))
                binding.strengthBar3.setBackgroundColor(getColor(R.color.border_default))
                binding.tvStrengthLabel.text = "Weak password"
                binding.tvStrengthLabel.setTextColor(getColor(R.color.error_red))
            }
            RegisterViewModel.PasswordStrength.MEDIUM -> {
                binding.strengthBar1.setBackgroundColor(getColor(R.color.warning_yellow))
                binding.strengthBar2.setBackgroundColor(getColor(R.color.warning_yellow))
                binding.strengthBar3.setBackgroundColor(getColor(R.color.border_default))
                binding.tvStrengthLabel.text = "Medium password"
                binding.tvStrengthLabel.setTextColor(getColor(R.color.warning_yellow))
            }
            RegisterViewModel.PasswordStrength.STRONG -> {
                binding.strengthBar1.setBackgroundColor(getColor(R.color.success_green))
                binding.strengthBar2.setBackgroundColor(getColor(R.color.success_green))
                binding.strengthBar3.setBackgroundColor(getColor(R.color.success_green))
                binding.tvStrengthLabel.text = "Strong password"
                binding.tvStrengthLabel.setTextColor(getColor(R.color.success_green))
            }
        }
    }
}