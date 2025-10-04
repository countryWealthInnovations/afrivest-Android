package com.afrivest.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afrivest.app.data.repository.AuthRepository
import com.afrivest.app.data.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import com.afrivest.app.data.local.SecurePreferences

@HiltViewModel
class OTPViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val securePreferences: SecurePreferences
) : ViewModel() {

    private val _email = MutableLiveData("")
    val email: LiveData<String> = _email

    private val _from = MutableLiveData("")
    val from: LiveData<String> = _from

    private val _otpCode = MutableLiveData("")
    val otpCode: LiveData<String> = _otpCode

    private val _timeRemaining = MutableLiveData(600) // 10 minutes
    val timeRemaining: LiveData<Int> = _timeRemaining

    private val _canResend = MutableLiveData(false)
    val canResend: LiveData<Boolean> = _canResend

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _navigateToDashboard = MutableLiveData(false)
    val navigateToDashboard: LiveData<Boolean> = _navigateToDashboard

    private var timerJob: kotlinx.coroutines.Job? = null

    fun initialize(email: String, from: String) {
        _email.value = email
        _from.value = from
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeRemaining.value!! > 0) {
                delay(1000)
                _timeRemaining.value = _timeRemaining.value!! - 1

                if (_timeRemaining.value == 0) {
                    _canResend.value = true
                }
            }
        }
    }

    fun getFormattedTime(): String {
        val time = _timeRemaining.value ?: 0
        val minutes = time / 60
        val seconds = time % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun onOTPChanged(code: String) {
        _otpCode.value = code
        if (code.length == 6) {
            verifyOTP()
        }
    }

    fun verifyOTP() {
        val code = _otpCode.value ?: return
        if (code.length != 6) return

        viewModelScope.launch {
            _isLoading.value = true

            try {
                val response = authRepository.verifyOTP(code)

                when (response) {
                    is Resource.Success -> {
                        _isLoading.value = false

                        // Update email verification status
                        securePreferences.setEmailVerified(true)

                        _navigateToDashboard.value = true
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = response.message
                    }
                    else -> {}
                }

            } catch (e: Exception) {
                Timber.e(e, "OTP verification failed")
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Verification failed. Please try again."
            }
        }
    }

    fun resendOTP() {
        if (_canResend.value != true) return

        viewModelScope.launch {
            _isLoading.value = true

            try {
                val response = authRepository.resendOTP()

                when (response) {
                    is Resource.Success -> {
                        _isLoading.value = false
                        _timeRemaining.value = 600
                        _canResend.value = false
                        _otpCode.value = ""
                        startTimer()
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = response.message
                    }
                    else -> {}
                }

            } catch (e: Exception) {
                Timber.e(e, "Resend OTP failed")
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Failed to resend code. Please try again."
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}