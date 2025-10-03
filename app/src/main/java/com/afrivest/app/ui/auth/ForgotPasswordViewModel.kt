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
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _email = MutableLiveData("")
    val email: LiveData<String> = _email

    private val _emailState = MutableLiveData<FieldState>(FieldState.Normal)
    val emailState: LiveData<FieldState> = _emailState

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isFormValid = MutableLiveData(false)
    val isFormValid: LiveData<Boolean> = _isFormValid

    private val _navigateToResetPassword = MutableLiveData(false)
    val navigateToResetPassword: LiveData<Boolean> = _navigateToResetPassword

    sealed class FieldState {
        object Normal : FieldState()
        object Valid : FieldState()
        data class Invalid(val message: String) : FieldState()
    }

    fun onEmailChanged(email: String) {
        _email.value = email
        validateEmail(email)
    }

    private fun validateEmail(email: String) {
        when {
            email.isEmpty() -> {
                _emailState.value = FieldState.Normal
                _isFormValid.value = false
            }
            Validators.isValidEmail(email) -> {
                _emailState.value = FieldState.Valid
                _isFormValid.value = true
            }
            else -> {
                _emailState.value = FieldState.Invalid("Please enter a valid email address")
                _isFormValid.value = false
            }
        }
    }

    fun sendResetCode() {
        if (_isFormValid.value != true) return

        viewModelScope.launch {
            _isLoading.value = true

            try {
                val response = authRepository.forgotPassword(_email.value ?: "")

                when (response) {
                    is Resource.Success -> {
                        _isLoading.value = false
                        _navigateToResetPassword.value = true
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = response.message
                    }
                    else -> {}
                }

            } catch (e: Exception) {
                Timber.e(e, "Send reset code failed")
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Failed to send reset code. Please try again."
            }
        }
    }
}