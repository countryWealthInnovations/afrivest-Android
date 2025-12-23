package com.afrivest.app.ui.marketplace

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afrivest.app.data.api.GoldPrice
import com.afrivest.app.data.api.InvestmentProduct
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.repository.MarketplaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _goldProducts = MutableLiveData<List<InvestmentProduct>>()
    val goldProducts: LiveData<List<InvestmentProduct>> = _goldProducts

    private val _goldPrice = MutableLiveData<GoldPrice?>()
    val goldPrice: LiveData<GoldPrice?> = _goldPrice

    fun loadGoldProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = marketplaceRepository.getGoldProducts()) {
                is Resource.Success -> {
                    _goldProducts.value = result.data ?: emptyList()
                    Timber.d("✅ Loaded ${result.data?.size ?: 0} gold products")
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message
                    Timber.e("❌ Failed to load gold products: ${result.message}")
                }
                is Resource.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }

    fun loadCurrentGoldPrice() {
        viewModelScope.launch {
            when (val result = marketplaceRepository.getCurrentGoldPrice()) {
                is Resource.Success -> {
                    _goldPrice.value = result.data
                    Timber.d("✅ Loaded gold price: ${result.data?.price_per_gram_ugx ?: 0} UGX/g")
                }
                is Resource.Error -> {
                    Timber.e("❌ Failed to load gold price: ${result.message}")
                }
                is Resource.Loading -> {
                    // Already handling loading state
                }
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}