package com.afrivest.app.ui.assets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.repository.InsuranceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FileClaimViewModel @Inject constructor(
    private val insuranceRepository: InsuranceRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> = _success

    fun fileClaim(
        policyId: Int,
        claimType: String,
        amount: String,
        description: String,
        incidentDate: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = insuranceRepository.fileClaim(
                policyId, claimType, amount, description, incidentDate
            )) {
                is Resource.Success -> {
                    _success.value = true
                    Timber.d("✅ Claim filed successfully")
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message
                    Timber.e("❌ Failed to file claim: ${result.message}")
                }
                is Resource.Loading -> {}
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}