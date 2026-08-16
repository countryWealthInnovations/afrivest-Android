package co.afrivest.ui.kyc

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.afrivest.data.local.SecurePreferences
import co.afrivest.data.model.Resource
import co.afrivest.data.repository.KycRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KycViewModel @Inject constructor(
    private val kycRepository: KycRepository,
    private val securePreferences: SecurePreferences
) : ViewModel() {

    enum class Phase { IDLE, CREATING_SESSION, VERIFYING, APPROVED, DECLINED, PENDING, CANCELLED, ERROR }

    private val _phase = MutableLiveData(Phase.IDLE)
    val phase: LiveData<Phase> = _phase

    private val _sessionToken = MutableLiveData<String?>(null)
    val sessionToken: LiveData<String?> = _sessionToken

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    fun createSession() {
        if (_phase.value == Phase.CREATING_SESSION || _phase.value == Phase.VERIFYING) return
        _phase.value = Phase.CREATING_SESSION
        viewModelScope.launch {
            when (val r = kycRepository.createSession()) {
                is Resource.Success -> {
                    val token = r.data?.session_token
                    if (token.isNullOrEmpty()) {
                        _errorMessage.value = "Verification session missing token"
                        _phase.value = Phase.ERROR
                    } else {
                        _sessionToken.value = token
                        _phase.value = Phase.VERIFYING
                    }
                }
                is Resource.Error -> {
                    _errorMessage.value = r.message
                    _phase.value = Phase.ERROR
                }
                else -> {}
            }
        }
    }

    fun onSdkApproved() { _phase.value = Phase.APPROVED; pollStatus() }
    fun onSdkDeclined() { _phase.value = Phase.DECLINED }
    fun onSdkPending() { _phase.value = Phase.PENDING; pollStatus() }
    fun onSdkCancelled() { _phase.value = Phase.CANCELLED }
    fun onSdkError(message: String?) { _phase.value = Phase.ERROR; _errorMessage.value = message }

    fun clearToken() { _sessionToken.value = null }
    fun clearError() { _errorMessage.value = null }

    // The final verdict is set by the Didit webhook on our backend, so confirm via status.
    private fun pollStatus() {
        viewModelScope.launch {
            repeat(6) {
                delay(2500)
                when (val r = kycRepository.getStatus()) {
                    is Resource.Success -> {
                        if (r.data?.verified == true) {
                            securePreferences.setKYCVerified(true)
                            _phase.value = Phase.APPROVED
                            return@launch
                        }
                        when (r.data?.status) {
                            "rejected" -> { _phase.value = Phase.DECLINED; return@launch }
                            "in_review", "review" -> _phase.value = Phase.PENDING
                            else -> {}
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}