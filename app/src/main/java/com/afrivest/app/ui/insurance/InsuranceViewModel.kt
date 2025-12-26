package com.afrivest.app.ui.insurance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afrivest.app.data.api.InsurancePolicy
import com.afrivest.app.data.api.InsuranceProvider
import com.afrivest.app.data.api.InvestmentProduct
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.repository.InsuranceRepository
import com.afrivest.app.data.repository.InvestmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class InsuranceViewModel @Inject constructor(
    private val insuranceRepository: InsuranceRepository,
    private val investmentRepository: InvestmentRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _providers = MutableLiveData<List<InsuranceProvider>>()
    val providers: LiveData<List<InsuranceProvider>> = _providers

    private val _insuranceProducts = MutableLiveData<List<InvestmentProduct>>()
    val insuranceProducts: LiveData<List<InvestmentProduct>> = _insuranceProducts

    private val _policies = MutableLiveData<List<InsurancePolicy>>()
    val policies: LiveData<List<InsurancePolicy>> = _policies

    private val _selectedProviderId = MutableLiveData<Int?>()

    fun loadProviders() {
        viewModelScope.launch {
            when (val result = insuranceRepository.getInsuranceProviders()) {
                is Resource.Success -> {
                    _providers.value = result.data ?: emptyList()
                    Timber.d("✅ Loaded ${result.data?.size ?: 0} insurance providers")
                }
                is Resource.Error -> {
                    Timber.e("❌ Failed to load providers: ${result.message}")
                }
                is Resource.Loading -> {
                    // Already handling loading state
                }
            }
        }
    }

    fun loadInsuranceProducts(providerId: Int? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedProviderId.value = providerId

            when (val result = investmentRepository.getInvestmentProducts(
                categorySlug = "insurance",
                riskLevel = null,
                sortBy = null
            )) {
                is Resource.Success -> {
                    var products = result.data ?: emptyList()

                    // Filter by provider if selected
                    if (providerId != null) {
                        products = products.filter { it.partner?.id == providerId }
                    }

                    _insuranceProducts.value = products
                    Timber.d("✅ Loaded ${products.size} insurance products")
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message
                    Timber.e("❌ Failed to load insurance products: ${result.message}")
                }
                is Resource.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }

    fun filterByProvider(providerId: Int?) {
        loadInsuranceProducts(providerId)
    }

    fun sortProducts(sortBy: String) {
        val currentProducts = _insuranceProducts.value ?: return

        val sortedProducts = when (sortBy) {
            "coverage_high" -> currentProducts.sortedByDescending { it.price?.toDoubleOrNull() ?: 0.0 }
            "coverage_low" -> currentProducts.sortedBy { it.price?.toDoubleOrNull() ?: 0.0 }
            "premium_low" -> currentProducts.sortedBy { it.min_investment?.toDoubleOrNull() ?: 0.0 }
            "premium_high" -> currentProducts.sortedByDescending { it.min_investment?.toDoubleOrNull()
                ?: 0.0 }
            else -> currentProducts
        }

        _insuranceProducts.value = sortedProducts
    }

    fun loadPolicies(status: String? = null, policyType: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = insuranceRepository.getInsurancePolicies(status, policyType)) {
                is Resource.Success -> {
                    _policies.value = result.data ?: emptyList()
                    Timber.d("✅ Loaded ${result.data?.size ?: 0} policies")
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message
                    Timber.e("❌ Failed to load policies: ${result.message}")
                }
                is Resource.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}