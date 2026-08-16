package co.afrivest.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.afrivest.data.local.SecurePreferences
import co.afrivest.data.model.Resource
import co.afrivest.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PhoneOTPViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val securePreferences: SecurePreferences
) : ViewModel() {

    private val _phone = MutableLiveData("")
    val phone: LiveData<String> = _phone

    private val _otpCode = MutableLiveData("")
    val otpCode: LiveData<String> = _otpCode

    private val _timeRemaining = MutableLiveData(600)
    val timeRemaining: LiveData<Int> = _timeRemaining

    private val _canResend = MutableLiveData(false)
    val canResend: LiveData<Boolean> = _canResend

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _verified = MutableLiveData(false)
    val verified: LiveData<Boolean> = _verified

    private var timerJob: Job? = null
    private var started = false

    fun initialize(phone: String) {
        if (started) return
        started = true
        _phone.value = phone
        sendOtp()
    }

    private fun startTimer() {
        timerJob?.cancel()
        _timeRemaining.value = 600
        _canResend.value = false
        timerJob = viewModelScope.launch {
            while ((_timeRemaining.value ?: 0) > 0) {
                delay(1000)
                _timeRemaining.value = (_timeRemaining.value ?: 0) - 1
                if (_timeRemaining.value == 0) _canResend.value = true
            }
        }
    }

    fun getFormattedTime(): String {
        val time = _timeRemaining.value ?: 0
        return String.format("%02d:%02d", time / 60, time % 60)
    }

    private fun sendOtp() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = authRepository.sendPhoneOtp()) {
                is Resource.Success -> startTimer()
                is Resource.Error -> _errorMessage.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun onOTPChanged(code: String) {
        _otpCode.value = code
        if (code.length == 6) verifyOTP()
    }

    fun verifyOTP() {
        val code = _otpCode.value ?: return
        if (code.length != 6) return
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = authRepository.verifyPhoneOtp(code)) {
                is Resource.Success -> {
                    securePreferences.setPhoneVerified(true)
                    _verified.value = true
                }
                is Resource.Error -> _errorMessage.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun resendOTP() {
        if (_canResend.value != true) return
        _otpCode.value = ""
        sendOtp()
    }

    fun clearError() { _errorMessage.value = null }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}