package co.afrivest.ui.investments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.afrivest.data.api.InvestmentProduct
import co.afrivest.data.api.PurchaseInvestmentRequest
import co.afrivest.data.api.InvestmentAgreementData
import co.afrivest.data.model.Resource
import co.afrivest.data.repository.AgreementRepository
import co.afrivest.data.repository.InvestmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val investmentRepository: InvestmentRepository,
    private val agreementRepository: AgreementRepository
) : ViewModel() {

    private val _agreementToShow = MutableLiveData<InvestmentAgreementData?>(null)
    val agreementToShow: LiveData<InvestmentAgreementData?> = _agreementToShow

    // Buffer the purchase intent while the agreement is shown
    private var pending: Triple<Int, Double, String>? = null
    private var pendingAutoReinvest = false

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

    // Checks agreement acceptance first. If not accepted, emits the agreement to show.
    fun startPurchase(productId: Int, amount: Double, currency: String, autoReinvest: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = agreementRepository.getAgreement()) {
                is Resource.Success -> {
                    val data = r.data
                    if (data != null && !data.accepted) {
                        pending = Triple(productId, amount, currency)
                        pendingAutoReinvest = autoReinvest
                        _isLoading.value = false
                        _agreementToShow.value = data
                    } else {
                        _isLoading.value = false
                        purchaseProduct(productId, amount, currency, autoReinvest)
                    }
                }
                is Resource.Error -> {
                    // No agreement configured or fetch failed: let the purchase attempt surface the server rule
                    _isLoading.value = false
                    purchaseProduct(productId, amount, currency, autoReinvest)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun acceptAgreementAndContinue() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = agreementRepository.accept()) {
                is Resource.Success -> {
                    _agreementToShow.value = null
                    val p = pending
                    if (p != null) {
                        purchaseProduct(p.first, p.second, p.third, pendingAutoReinvest)
                        pending = null
                    } else {
                        _isLoading.value = false
                    }
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    _errorMessage.value = r.message
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun dismissAgreement() { _agreementToShow.value = null }

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