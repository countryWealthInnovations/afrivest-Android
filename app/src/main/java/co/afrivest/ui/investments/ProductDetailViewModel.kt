package co.afrivest.ui.investments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.afrivest.data.api.InvestmentProduct
import co.afrivest.data.api.PurchaseInvestmentRequest
import co.afrivest.data.model.Resource
import co.afrivest.data.repository.InvestmentRepository
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

    private val _product = MutableLiveData<InvestmentProduct>()
    val product: LiveData<InvestmentProduct> = _product

    fun loadFullProduct(slug: String) {
        viewModelScope.launch {
            when (val result = investmentRepository.getInvestmentProduct(slug)) {
                is Resource.Success -> {
                    result.data?.let { _product.value = it }
                }
                is Resource.Error -> Timber.w("Could not fetch full product: ${result.message}")
                is Resource.Loading -> {}
            }
        }
    }

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