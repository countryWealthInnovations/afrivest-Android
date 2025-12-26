package com.afrivest.app.ui.assets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afrivest.app.data.api.InsurancePolicy
import com.afrivest.app.data.api.UserInvestment
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.repository.InsuranceRepository
import com.afrivest.app.data.repository.InvestmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AssetsViewModel @Inject constructor(
    private val investmentRepository: InvestmentRepository,
    private val insuranceRepository: InsuranceRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _investments = MutableLiveData<List<UserInvestment>>()
    val investments: LiveData<List<UserInvestment>> = _investments

    private val _policies = MutableLiveData<List<InsurancePolicy>>()
    val policies: LiveData<List<InsurancePolicy>> = _policies

    fun loadData() {
        loadInvestments()
        loadPolicies()
    }

    fun loadInvestments() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = investmentRepository.getUserInvestments(status = null)) {
                is Resource.Success -> {
                    _investments.value = result.data ?: emptyList()
                    Timber.d("✅ Loaded ${result.data?.size ?: 0} investments")
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message
                    Timber.e("❌ Failed to load investments: ${result.message}")
                }
                is Resource.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }

    fun loadPolicies() {
        viewModelScope.launch {
            when (val result = insuranceRepository.getInsurancePolicies(status = null, policyType = null)) {
                is Resource.Success -> {
                    _policies.value = result.data ?: emptyList()
                    Timber.d("✅ Loaded ${result.data?.size ?: 0} policies")
                }
                is Resource.Error -> {
                    Timber.e("❌ Failed to load policies: ${result.message}")
                }
                is Resource.Loading -> {
                    // Already handling loading state
                }
            }
        }
    }

    val totalInvestmentValue: Double
        get() = investments.value?.sumOf { it.current_value.toDoubleOrNull() ?: 0.0 } ?: 0.0

    val totalReturns: Double
        get() = investments.value?.sumOf {
            val invested = it.amount_invested.toDoubleOrNull() ?: 0.0
            val current = it.current_value.toDoubleOrNull() ?: 0.0
            current - invested
        } ?: 0.0

    fun clearError() {
        _errorMessage.value = null
    }
}