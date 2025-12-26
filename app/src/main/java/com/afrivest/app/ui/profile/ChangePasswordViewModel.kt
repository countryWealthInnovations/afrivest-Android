package com.afrivest.app.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.repository.ProfileRepository
import com.afrivest.app.utils.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _currentPassword = MutableLiveData("")
    val currentPassword: LiveData<String> = _currentPassword

    private val _newPassword = MutableLiveData("")
    val newPassword: LiveData<String> = _newPassword

    private val _confirmPassword = MutableLiveData("")
    val confirmPassword: LiveData<String> = _confirmPassword

    private val _currentPasswordState = MutableLiveData<FieldState>(FieldState.Normal)
    val currentPasswordState: LiveData<FieldState> = _currentPasswordState

    private val _newPasswordState = MutableLiveData<FieldState>(FieldState.Normal)
    val newPasswordState: LiveData<FieldState> = _newPasswordState

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

    private val _changePasswordSuccess = MutableLiveData(false)
    val changePasswordSuccess: LiveData<Boolean> = _changePasswordSuccess

    sealed class FieldState {
        object Normal : FieldState()
        object Valid : FieldState()
        data class Invalid(val message: String) : FieldState()
    }

    enum class PasswordStrength {
        WEAK, MEDIUM, STRONG
    }

    fun onCurrentPasswordChanged(password: String) {
        _currentPassword.value = password
        validateCurrentPassword(password)
        checkFormValidity()
    }

    fun onNewPasswordChanged(password: String) {
        _newPassword.value = password
        validateNewPassword(password)
        validateConfirmPassword()
        checkFormValidity()
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _confirmPassword.value = confirmPassword
        validateConfirmPassword()
        checkFormValidity()
    }

    private fun validateCurrentPassword(password: String) {
        when {
            password.isEmpty() -> _currentPasswordState.value = FieldState.Normal
            password.length >= 8 -> _currentPasswordState.value = FieldState.Valid
            else -> _currentPasswordState.value = FieldState.Invalid("Password is required")
        }
    }

    private fun validateNewPassword(password: String) {
        when {
            password.isEmpty() -> {
                _newPasswordState.value = FieldState.Normal
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
                        _newPasswordState.value = FieldState.Valid
                    }
                    is com.afrivest.app.utils.ValidationResult.Error -> {
                        _newPasswordState.value = FieldState.Invalid(result.messages.joinToString(", "))
                    }
                }
            }
        }
    }

    private fun validateConfirmPassword() {
        val newPassword = _newPassword.value ?: ""
        val confirmPassword = _confirmPassword.value ?: ""

        when {
            confirmPassword.isEmpty() -> _confirmPasswordState.value = FieldState.Normal
            confirmPassword == newPassword -> _confirmPasswordState.value = FieldState.Valid
            else -> _confirmPasswordState.value = FieldState.Invalid("Passwords do not match")
        }
    }

    private fun checkFormValidity() {
        val isValid = _currentPasswordState.value is FieldState.Valid &&
                _newPasswordState.value is FieldState.Valid &&
                _confirmPasswordState.value is FieldState.Valid

        _isFormValid.value = isValid
    }

    fun changePassword() {
        if (_isFormValid.value != true) return

        viewModelScope.launch {
            _isLoading.value = true

            try {
                when (val response = profileRepository.updatePassword(
                    currentPassword = _currentPassword.value ?: "",
                    newPassword = _newPassword.value ?: "",
                    newPasswordConfirmation = _confirmPassword.value ?: ""
                )) {
                    is Resource.Success -> {
                        _isLoading.value = false
                        _changePasswordSuccess.value = true
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = response.message
                    }
                    is Resource.Loading -> {
                        // Already handled by _isLoading
                    }
                }

            } catch (e: Exception) {
                Timber.e(e, "Change password failed")
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Failed to change password. Please try again."
            }
        }
    }
}