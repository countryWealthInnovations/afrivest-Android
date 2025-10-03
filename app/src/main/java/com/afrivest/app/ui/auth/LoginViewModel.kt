package com.afrivest.app.ui.auth

import androidx.biometric.BiometricManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afrivest.app.data.local.SecurePreferences
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.repository.AuthRepository
import com.afrivest.app.utils.Validators
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val securePreferences: SecurePreferences,
    private val biometricManager: BiometricManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _shouldPromptBiometricSetup = MutableLiveData(false)
    val shouldPromptBiometricSetup: LiveData<Boolean> = _shouldPromptBiometricSetup
    // Form Fields
    private val _email = MutableLiveData("")
    val email: LiveData<String> = _email

    private val _password = MutableLiveData("")
    val password: LiveData<String> = _password

    // Field States
    private val _emailState = MutableLiveData<FieldState>(FieldState.Normal)
    val emailState: LiveData<FieldState> = _emailState

    private val _passwordState = MutableLiveData<FieldState>(FieldState.Normal)
    val passwordState: LiveData<FieldState> = _passwordState

    // UI States
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isFormValid = MutableLiveData(false)
    val isFormValid: LiveData<Boolean> = _isFormValid

    // Navigation
    private val _navigateToOTP = MutableLiveData(false)
    val navigateToOTP: LiveData<Boolean> = _navigateToOTP

    private val _navigateToDashboard = MutableLiveData(false)
    val navigateToDashboard: LiveData<Boolean> = _navigateToDashboard

    // Field State Sealed Class
    sealed class FieldState {
        object Normal : FieldState()
        object Valid : FieldState()
        data class Invalid(val message: String) : FieldState()
    }

    // MARK: - Input Handlers
    fun onEmailChanged(email: String) {
        _email.value = email
        validateEmail(email)
        checkFormValidity()
    }

    fun onPasswordChanged(password: String) {
        _password.value = password
        checkFormValidity()
    }

    // MARK: - Validation Methods
    private fun validateEmail(email: String) {
        when {
            email.isEmpty() -> _emailState.value = FieldState.Normal
            Validators.isValidEmail(email) -> _emailState.value = FieldState.Valid
            else -> _emailState.value = FieldState.Invalid("Please enter a valid email address")
        }
    }

    private fun checkFormValidity() {
        val isValid = _emailState.value is FieldState.Valid &&
                !(_password.value.isNullOrEmpty())

        _isFormValid.value = isValid
    }

    // MARK: - Login
    fun login() {
        if (_isFormValid.value != true) return

        viewModelScope.launch {
            _isLoading.value = true

            try {
                val deviceToken = FirebaseMessaging.getInstance().token.await()
                securePreferences.saveDeviceToken(deviceToken)

                val response = authRepository.login(
                    email = _email.value ?: "",
                    password = _password.value ?: "",
                    deviceToken = deviceToken
                )

                when (response) {
                    is Resource.Success -> {
                        val authResponse = response.data!!
                        val emailVerified = authResponse.user.isVerified()

                        _isLoading.value = false

                        if (emailVerified) {
                            _navigateToDashboard.value = true
                        } else {
                            _navigateToOTP.value = true
                        }
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = response.message
                    }
                    else -> {}
                }

            } catch (e: Exception) {
                Timber.e(e, "Login failed")
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Login failed. Please try again."
            }
        }
    }

    // MARK: - Biometric
    fun isBiometricAvailable(): Boolean {
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS &&
                securePreferences.isBiometricEnabled()
    }

    fun loginWithBiometric() {
        viewModelScope.launch {
            val savedEmail = securePreferences.getUserEmail()
            if (savedEmail != null) {
                _email.value = savedEmail
                // In production, use token-based auth or retrieve from secure storage
                login()
            }
        }
    }

    fun enableBiometric() {
        securePreferences.setBiometricEnabled(true)
        _shouldPromptBiometricSetup.value = false
    }

    fun skipBiometric() {
        _shouldPromptBiometricSetup.value = false
    }
}