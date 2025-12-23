package com.afrivest.app.ui.investments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afrivest.app.data.api.InvestmentCategory
import com.afrivest.app.data.api.InvestmentProduct
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.repository.InvestmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class InvestmentViewModel @Inject constructor(
    private val investmentRepository: InvestmentRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _categories = MutableLiveData<List<InvestmentCategory>>()
    val categories: LiveData<List<InvestmentCategory>> = _categories

    private val _products = MutableLiveData<List<InvestmentProduct>>()
    val products: LiveData<List<InvestmentProduct>> = _products

    private val _selectedProduct = MutableLiveData<InvestmentProduct?>()
    val selectedProduct: LiveData<InvestmentProduct?> = _selectedProduct

    private val _selectedCategorySlug = MutableLiveData<String?>()
    val selectedCategorySlug: LiveData<String?> = _selectedCategorySlug

    fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = investmentRepository.getInvestmentCategories()) {
                is Resource.Success -> {
                    _categories.value = result.data ?: emptyList()
                    Timber.d("✅ Loaded ${result.data?.size ?: 0} categories")
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message
                    Timber.e("❌ Failed to load categories: ${result.message}")
                }
                is Resource.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }

    fun loadProducts(categorySlug: String? = null, riskLevel: String? = null, sortBy: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedCategorySlug.value = categorySlug

            when (val result = investmentRepository.getInvestmentProducts(categorySlug, riskLevel, sortBy)) {
                is Resource.Success -> {
                    _products.value = result.data ?: emptyList()
                    Timber.d("✅ Loaded ${result.data?.size ?: 0} products")
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message
                    Timber.e("❌ Failed to load products: ${result.message}")
                }
                is Resource.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }

    fun loadFeaturedProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = investmentRepository.getFeaturedProducts()) {
                is Resource.Success -> {
                    _products.value = result.data ?: emptyList()
                    Timber.d("✅ Loaded ${result.data?.size ?: 0} featured products")
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message
                    Timber.e("❌ Failed to load featured products: ${result.message}")
                }
                is Resource.Loading -> {
                    // Already handling loading state
                }
            }
            _isLoading.value = false
        }
    }

    fun loadProductDetails(slug: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = investmentRepository.getInvestmentProduct(slug)) {
                is Resource.Success -> {
                    _selectedProduct.value = result.data
                    Timber.d("✅ Loaded product: ${result.data?.name ?: "Unknown"}")
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message
                    Timber.e("❌ Failed to load product: ${result.message}")
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

    fun filterByCategory(categorySlug: String?) {
        loadProducts(categorySlug = categorySlug)
    }

    fun sortProducts(sortBy: String) {
        loadProducts(
            categorySlug = _selectedCategorySlug.value,
            sortBy = sortBy
        )
    }
}