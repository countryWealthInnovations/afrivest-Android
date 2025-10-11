package com.afrivest.app.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.model.Transaction
import com.afrivest.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
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
                        _transactions.value = allTransactions.toList()
                        Timber.d("✅ Loaded ${transactions.size} transactions")
                    }
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

    /**
     * Filter transactions by status
     */
    fun filterTransactions(status: String?) {
        currentFilter = status
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