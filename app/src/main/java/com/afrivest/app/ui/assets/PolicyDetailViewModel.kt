package com.afrivest.app.ui.assets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afrivest.app.data.api.InsuranceClaim
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.repository.InsuranceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PolicyDetailViewModel @Inject constructor(
    private val insuranceRepository: InsuranceRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _claims = MutableLiveData<List<InsuranceClaim>>()
    val claims: LiveData<List<InsuranceClaim>> = _claims

    fun loadClaims(policyId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = insuranceRepository.getClaims(policyId)) {
                is Resource.Success -> {
                    _claims.value = result.data ?: emptyList()
                    Timber.d("✅ Loaded ${result.data?.size ?: 0} claims")
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message
                    Timber.e("❌ Failed to load claims: ${result.message}")
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