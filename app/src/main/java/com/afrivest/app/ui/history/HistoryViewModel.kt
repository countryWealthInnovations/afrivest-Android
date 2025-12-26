package com.afrivest.app.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.model.Transaction
import com.afrivest.app.data.repository.InvestmentRepository
import com.afrivest.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val investmentRepository: InvestmentRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _isLastPage = MutableLiveData(false)
    val isLastPage: LiveData<Boolean> = _isLastPage

    // Private list to hold all transactions
    private val allTransactions = mutableListOf<Transaction>()

    // Current filter
    private var currentFilter: String? = null
    private var currentPage = 1
    private var totalPages = 1

    /**
     * Load transactions (initial load)
     */
    fun loadTransactions() {
        // Prevent concurrent requests
        if (_isLoading.value == true) {
            Timber.d("⏸️ Already loading, skipping request")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            currentPage = 1
            allTransactions.clear()

            when (val result = transactionRepository.getTransactions(
                page = currentPage,
                perPage = 20,
                status = currentFilter
            )) {
                is Resource.Success -> {
                    result.data?.let { transactions ->
                        allTransactions.addAll(transactions)
                    }

                    // Load investment purchases
                    loadInvestmentPurchases()

                    _transactions.value = allTransactions.sortedByDescending {
                        parseDate(it.created_at)
                    }
                    Timber.d("✅ Loaded ${allTransactions.size} total transactions")
                    _isLastPage.value = true
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message
                    Timber.e("❌ Failed to load transactions: ${result.message}")
                }
                is Resource.Loading -> {
                    // Handle loading if needed
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Load more transactions (pagination)
     */
    fun loadMoreTransactions() {
        if (_isLoading.value == true || _isLastPage.value == true) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            currentPage++

            when (val result = transactionRepository.getTransactions(
                page = currentPage,
                perPage = 20,
                status = currentFilter
            )) {
                is Resource.Success -> {
                    if (result.data!!.isEmpty()) {
                        _isLastPage.value = true
                    } else {
                        allTransactions.addAll(result.data)
                        _transactions.value = allTransactions.toList()
                    }

                    Timber.d("✅ Loaded more transactions: page $currentPage")
                }
                is Resource.Error -> {
                    currentPage-- // Revert page increment on error
                    _errorMessage.value = result.message
                    Timber.e("❌ Failed to load more transactions: ${result.message}")
                }
                is Resource.Loading -> {
                    // Handle loading if needed
                }
            }

            _isLoading.value = false
        }
    }


    private suspend fun loadInvestmentPurchases() {
        when (val result = investmentRepository.getUserInvestments(status = null)) {
            is Resource.Success -> {
                result.data?.forEach { investment ->
                    val transaction = Transaction(
                        id = -investment.id, // Negative ID to avoid conflicts
                        reference = investment.investment_code,
                        type = "investment",
                        amount = investment.amount_invested,
                        fee_amount = null,
                        total_amount = investment.amount_invested,
                        currency = investment.currency,
                        status = investment.status,
                        payment_channel = "wallet",
                        external_reference = null,
                        description = "Investment: ${investment.product?.title}",
                        direction = "sent",
                        other_party = null,
                        user = null,
                        wallet = null,
                        recipient = null,
                        created_at = investment.purchase_date,
                        updated_at = null,
                        completed_at = investment.purchase_date
                    )
                    allTransactions.add(transaction)
                }
            }
            is Resource.Error -> {
                Timber.e("❌ Failed to load investments: ${result.message}")
            }
            is Resource.Loading -> {}
        }
    }

    private fun parseDate(dateString: String): Long {
        return try {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", java.util.Locale.US)
            format.timeZone = java.util.TimeZone.getTimeZone("UTC")
            format.parse(dateString)?.time ?: 0
        } catch (e: Exception) {
            0
        }
    }
    /**
     * Filter transactions by status
     */
    fun filterTransactions(status: String?) {
        // Don't reload if filter hasn't changed or already loading
        if (currentFilter == status || _isLoading.value == true) {
            return
        }

        currentFilter = status
        allTransactions.clear()
        loadTransactions()
    }

    /**
     * Refresh transactions
     */
    fun refreshTransactions() {
        loadTransactions()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}