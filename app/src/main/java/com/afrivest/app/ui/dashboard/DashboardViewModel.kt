package com.afrivest.app.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afrivest.app.data.model.Dashboard
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.model.Transaction
import com.afrivest.app.data.model.User
import com.afrivest.app.data.model.Wallet
import com.afrivest.app.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _dashboard = MutableLiveData<Dashboard?>()
    val dashboard: LiveData<Dashboard?> = _dashboard

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _wallets = MutableLiveData<List<Wallet>>()
    val wallets: LiveData<List<Wallet>> = _wallets

    private val _recentTransactions = MutableLiveData<List<Transaction>>()
    val recentTransactions: LiveData<List<Transaction>> = _recentTransactions

    // UI State
    private val _isAmountHidden = MutableLiveData(false)
    val isAmountHidden: LiveData<Boolean> = _isAmountHidden

    private val _isOtherCurrenciesExpanded = MutableLiveData(false)
    val isOtherCurrenciesExpanded: LiveData<Boolean> = _isOtherCurrenciesExpanded

    private val _greeting = MutableLiveData<String>()
    val greeting: LiveData<String> = _greeting

    init {
        updateGreeting()
    }

    /**
     * Load dashboard data
     */
    fun loadDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = walletRepository.getDashboard()) {
                is Resource.Success -> {
                    _dashboard.value = result.data
                    _user.value = result.data?.user
                    _wallets.value = result.data?.wallets ?: emptyList()
                    _recentTransactions.value = result.data?.recentTransactions ?: emptyList()
                    _isLoading.value = false
                }
                is Resource.Loading -> {
                    // Handle loading state if needed
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message
                    _isLoading.value = false

                    // Fallback to wallets endpoint
                    loadWalletsFallback()
                }
            }
        }
    }

    /**
     * Fallback to wallets endpoint
     */
    private fun loadWalletsFallback() {
        viewModelScope.launch {
            when (val result = walletRepository.getWallets()) {
                is Resource.Success -> {
                    _wallets.value = result.data ?: emptyList()
                }
                is Resource.Loading -> {
                    // Handle loading state if needed
                }
                is Resource.Error -> {
                    _errorMessage.value = "Failed to load data: ${result.message}"
                }
            }
        }
    }

    /**
     * Update greeting based on current time
     */
    fun updateGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        _greeting.value = when (hour) {
            in 5..11 -> "Good morning!"
            in 12..16 -> "Good afternoon!"
            in 17..20 -> "Good evening!"
            else -> "Good night!"
        }
    }

    /**
     * Toggle amount visibility
     */
    fun toggleAmountVisibility() {
        _isAmountHidden.value = !(_isAmountHidden.value ?: false)
    }

    /**
     * Toggle other currencies expansion
     */
    fun toggleOtherCurrencies() {
        _isOtherCurrenciesExpanded.value = !(_isOtherCurrenciesExpanded.value ?: false)
    }

    /**
     * Get deposit wallet (UGX only) - All UGX wallets go to deposit
     */
    fun getDepositWallet(): Wallet? {
        return _wallets.value?.firstOrNull { it.currency == "UGX" }
    }

    /**
     * Get interest wallet - Always null (unpopulated by default)
     */
    fun getInterestWallet(): Wallet? {
        return null
    }

    /**
     * Get other currency wallets (non-UGX)
     */
    fun getOtherCurrencyWallets(): List<Wallet> {
        return _wallets.value?.filter { it.currency != "UGX" } ?: emptyList()
    }

    /**
     * Format balance with masking option
     */
    fun formatBalance(balance: String, currency: String): String {
        if (_isAmountHidden.value == true) {
            return "•• $currency"
        }

        val amount = balance.toDoubleOrNull() ?: 0.0
        return String.format("%,.2f %s", amount, currency)
    }

    /**
     * Refresh data
     */
    fun refresh() {
        loadDashboard()
        updateGreeting()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}