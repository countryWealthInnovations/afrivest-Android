package com.afrivest.app.ui.investments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afrivest.app.data.api.InvestmentProduct
import com.afrivest.app.data.api.PurchaseInvestmentRequest
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.repository.InvestmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val investmentRepository: InvestmentRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _purchaseSuccess = MutableLiveData<Boolean>()
    val purchaseSuccess: LiveData<Boolean> = _purchaseSuccess

    fun purchaseProduct(productId: Int, amount: Double, currency: String, autoReinvest: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true

            val request = PurchaseInvestmentRequest(
                product_id = productId,
                amount = amount,
                currency = currency,
                payout_frequency = "monthly",
                auto_reinvest = autoReinvest
            )

            when (val result = investmentRepository.purchaseInvestment(request)) {
                is Resource.Success -> {
                    _purchaseSuccess.value = true
                    Timber.d("✅ Purchase successful")
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message ?: "Purchase failed"
                    _purchaseSuccess.value = false
                    Timber.e("❌ Purchase failed: ${result.message}")
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