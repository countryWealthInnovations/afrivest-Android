package com.afrivest.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afrivest.app.data.local.SecurePreferences
import com.afrivest.app.data.repository.AuthRepository
import com.afrivest.app.data.model.Resource
import com.afrivest.app.ui.auth.models.Country
import com.afrivest.app.utils.Validators
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val securePreferences: SecurePreferences,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Form Fields
    private val _name = MutableLiveData("")
    val name: LiveData<String> = _name

    private val _email = MutableLiveData("")
    val email: LiveData<String> = _email

    private val _phoneNumber = MutableLiveData("")
    val phoneNumber: LiveData<String> = _phoneNumber

    private val _password = MutableLiveData("")
    val password: LiveData<String> = _password

    private val _confirmPassword = MutableLiveData("")
    val confirmPassword: LiveData<String> = _confirmPassword

    private val _termsAccepted = MutableLiveData(false)
    val termsAccepted: LiveData<Boolean> = _termsAccepted

    private val _selectedCountry = MutableLiveData(Country.DEFAULT)
    val selectedCountry: LiveData<Country> = _selectedCountry

    // Field States
    private val _nameState = MutableLiveData<FieldState>(FieldState.Normal)
    val nameState: LiveData<FieldState> = _nameState

    private val _emailState = MutableLiveData<FieldState>(FieldState.Normal)
    val emailState: LiveData<FieldState> = _emailState

    private val _phoneState = MutableLiveData<FieldState>(FieldState.Normal)
    val phoneState: LiveData<FieldState> = _phoneState

    private val _passwordState = MutableLiveData<FieldState>(FieldState.Normal)
    val passwordState: LiveData<FieldState> = _passwordState

    private val _confirmPasswordState = MutableLiveData<FieldState>(FieldState.Normal)
    val confirmPasswordState: LiveData<FieldState> = _confirmPasswordState

    private val _passwordStrength = MutableLiveData<PasswordStrength>(PasswordStrength.WEAK)
    val passwordStrength: LiveData<PasswordStrength> = _passwordStrength

    // UI States
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isFormValid = MutableLiveData(false)
    val isFormValid: LiveData<Boolean> = _isFormValid

    private val _navigateToOTP = MutableLiveData(false)
    val navigateToOTP: LiveData<Boolean> = _navigateToOTP

    // Field State Sealed Class
    sealed class FieldState {
        object Normal : FieldState()
        object Valid : FieldState()
        data class Invalid(val message: String) : FieldState()
    }

    // Password Strength
    enum class PasswordStrength {
        WEAK, MEDIUM, STRONG
    }

    // MARK: - Input Handlers
    fun onNameChanged(name: String) {
        _name.value = name
        validateName(name)
        checkFormValidity()
    }

    fun onEmailChanged(email: String) {
        _email.value = email
        validateEmail(email)
        checkFormValidity()
    }

    fun onPhoneChanged(phone: String) {
        _phoneNumber.value = phone
        validatePhone(phone)
        checkFormValidity()
    }

    fun onPasswordChanged(password: String) {
        _password.value = password
        validatePassword(password)
        validateConfirmPassword()
        checkFormValidity()
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _confirmPassword.value = confirmPassword
        validateConfirmPassword()
        checkFormValidity()
    }

    fun onTermsAccepted(accepted: Boolean) {
        _termsAccepted.value = accepted
        checkFormValidity()
    }

    fun onCountrySelected(country: Country) {
        _selectedCountry.value = country
        validatePhone(_phoneNumber.value ?: "")
    }

    // MARK: - Validation Methods
    private fun validateName(name: String) {
        when {
            name.isEmpty() -> _nameState.value = FieldState.Normal
            Validators.isValidName(name) -> _nameState.value = FieldState.Valid
            else -> _nameState.value = FieldState.Invalid("Please enter your full name (first and last name)")
        }
    }

    private fun validateEmail(email: String) {
        when {
            email.isEmpty() -> _emailState.value = FieldState.Normal
            Validators.isValidEmail(email) -> _emailState.value = FieldState.Valid
            else -> _emailState.value = FieldState.Invalid("Please enter a valid email address")
        }
    }

    private fun validatePhone(phone: String) {
        when {
            phone.isEmpty() -> _phoneState.value = FieldState.Normal
            else -> {
                val fullPhone = _selectedCountry.value?.dialCode?.replace("+", "") + phone
                if (Validators.isValidPhoneNumber(fullPhone)) {
                    _phoneState.value = FieldState.Valid
                } else {
                    _phoneState.value = FieldState.Invalid("Please enter a valid phone number")
                }
            }
        }
    }

    private fun validatePassword(password: String) {
        when {
            password.isEmpty() -> {
                _passwordState.value = FieldState.Normal
                _passwordStrength.value = PasswordStrength.WEAK
            }
            else -> {
                val result = Validators.isValidPassword(password)
                val strength = Validators.getPasswordStrength(password)

                _passwordStrength.value = when (strength) {
                    com.afrivest.app.utils.PasswordStrength.WEAK -> PasswordStrength.WEAK
                    com.afrivest.app.utils.PasswordStrength.MEDIUM -> PasswordStrength.MEDIUM
                    com.afrivest.app.utils.PasswordStrength.STRONG -> PasswordStrength.STRONG
                }

                when (result) {
                    is com.afrivest.app.utils.ValidationResult.Success -> {
                        _passwordState.value = FieldState.Valid
                    }
                    is com.afrivest.app.utils.ValidationResult.Error -> {
                        _passwordState.value = FieldState.Invalid(result.messages.joinToString(", "))
                    }
                }
            }
        }
    }

    private fun validateConfirmPassword() {
        val password = _password.value ?: ""
        val confirmPassword = _confirmPassword.value ?: ""

        when {
            confirmPassword.isEmpty() -> _confirmPasswordState.value = FieldState.Normal
            confirmPassword == password -> _confirmPasswordState.value = FieldState.Valid
            else -> _confirmPasswordState.value = FieldState.Invalid("Passwords do not match")
        }
    }

    private fun checkFormValidity() {
        val isValid = _nameState.value is FieldState.Valid &&
                _emailState.value is FieldState.Valid &&
                _phoneState.value is FieldState.Valid &&
                _passwordState.value is FieldState.Valid &&
                _confirmPasswordState.value is FieldState.Valid &&
                (_termsAccepted.value == true)

        _isFormValid.value = isValid
    }

    // MARK: - Register
    fun register() {
        if (_isFormValid.value != true) return

        viewModelScope.launch {
            _isLoading.value = true

            try {
                val deviceToken = FirebaseMessaging.getInstance().token.await()

                // Prepare phone number
                val fullPhone = _selectedCountry.value?.dialCode?.replace("+", "") + _phoneNumber.value

                val response = authRepository.register(
                    name = _name.value ?: "",
                    email = _email.value ?: "",
                    phoneNumber = fullPhone ?: "",
                    password = _password.value ?: "",
                    deviceToken = deviceToken
                )

                when (response) {
                    is Resource.Success -> {
                        _isLoading.value = false
                        _navigateToOTP.value = true
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = response.message
                    }
                    else -> {}
                }

            } catch (e: Exception) {
                Timber.e(e, "Registration failed")
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Registration failed. Please try again."
            }
        }
    }
}