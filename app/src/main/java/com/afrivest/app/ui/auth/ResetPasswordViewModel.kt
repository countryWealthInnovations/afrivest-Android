package com.afrivest.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afrivest.app.data.repository.AuthRepository
import com.afrivest.app.data.model.Resource
import com.afrivest.app.utils.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _email = MutableLiveData("")
    val email: LiveData<String> = _email

    private val _code = MutableLiveData("")
    val code: LiveData<String> = _code

    private val _password = MutableLiveData("")
    val password: LiveData<String> = _password

    private val _confirmPassword = MutableLiveData("")
    val confirmPassword: LiveData<String> = _confirmPassword

    private val _codeState = MutableLiveData<FieldState>(FieldState.Normal)
    val codeState: LiveData<FieldState> = _codeState

    private val _passwordState = MutableLiveData<FieldState>(FieldState.Normal)
    val passwordState: LiveData<FieldState> = _passwordState

    private val _confirmPasswordState = MutableLiveData<FieldState>(FieldState.Normal)
    val confirmPasswordState: LiveData<FieldState> = _confirmPasswordState

    private val _passwordStrength = MutableLiveData<PasswordStrength>(PasswordStrength.WEAK)
    val passwordStrength: LiveData<PasswordStrength> = _passwordStrength

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isFormValid = MutableLiveData(false)
    val isFormValid: LiveData<Boolean> = _isFormValid

    private val _navigateToLogin = MutableLiveData(false)
    val navigateToLogin: LiveData<Boolean> = _navigateToLogin

    sealed class FieldState {
        object Normal : FieldState()
        object Valid : FieldState()
        data class Invalid(val message: String) : FieldState()
    }

    enum class PasswordStrength {
        WEAK, MEDIUM, STRONG
    }

    fun initialize(email: String) {
        _email.value = email
    }

    fun onCodeChanged(code: String) {
        _code.value = code
        validateCode(code)
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

    private fun validateCode(code: String) {
        when {
            code.isEmpty() -> _codeState.value = FieldState.Normal
            code.length == 6 -> _codeState.value = FieldState.Valid
            else -> _codeState.value = FieldState.Invalid("Code must be 6 digits")
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
        val isValid = _codeState.value is FieldState.Valid &&
                _passwordState.value is FieldState.Valid &&
                _confirmPasswordState.value is FieldState.Valid

        _isFormValid.value = isValid
    }

    fun resetPassword() {
        if (_isFormValid.value != true) return

        viewModelScope.launch {
            _isLoading.value = true

            try {
                val response = authRepository.resetPassword(
                    email = _email.value ?: "",
                    code = _code.value ?: "",
                    password = _password.value ?: ""
                )

                when (response) {
                    is Resource.Success -> {
                        _isLoading.value = false
                        _navigateToLogin.value = true
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = response.message
                    }
                    else -> {}
                }

            } catch (e: Exception) {
                Timber.e(e, "Reset password failed")
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Failed to reset password. Please try again."
            }
        }
    }
}