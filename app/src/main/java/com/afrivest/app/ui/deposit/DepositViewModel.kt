package com.afrivest.app.ui.deposit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.repository.DepositRepository
import com.afrivest.app.data.repository.SDKInitiateResponse
import com.afrivest.app.utils.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DepositViewModel @Inject constructor(
    private val repository: DepositRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    private val _initiatedTransaction = MutableLiveData<SDKInitiateResponse?>()
    val initiatedTransaction: LiveData<SDKInitiateResponse?> = _initiatedTransaction

    private val _navigateToDashboard = MutableLiveData<Boolean>(false)
    val navigateToDashboard: LiveData<Boolean> = _navigateToDashboard

    private var _phoneNumber: String = ""

    fun getPhoneNumber(): String = _phoneNumber

    fun validateAndInitiateDeposit(amount: Double, currency: String, phoneNumber: String) {
        if (amount < 1000) {
            _errorMessage.value = "Minimum amount is 1,000"
            return
        }

        // Validate phone number
        if (!Validators.isValidPhoneNumber(phoneNumber)) {
            _errorMessage.value = "Please enter a valid phone number"
            return
        }

        // Store phone for later use
        _phoneNumber = phoneNumber

        initiateDeposit(amount, currency)
    }

    private fun initiateDeposit(amount: Double, currency: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            when (val result = repository.initiateSDKDeposit(amount, currency)) {
                is Resource.Success -> {
                    _isLoading.value = false
                    _initiatedTransaction.value = result.data
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    _errorMessage.value = result.message
                }
                is Resource.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    fun getInitiatedTransaction(): SDKInitiateResponse? = _initiatedTransaction.value

    fun verifyDeposit(flwRef: String, status: String) {
        val transaction = _initiatedTransaction.value ?: run {
            _errorMessage.value = "No transaction to verify"
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            when (val result = repository.verifySDKDeposit(
                transaction.transaction_id,
                flwRef,
                status
            )) {
                is Resource.Success -> {
                    _isLoading.value = false

                    result.data?.let { data ->
                        if (data.transaction.status == "success") {
                            _successMessage.value = "Deposit successful! ${data.transaction.amount} ${data.transaction.currency}"
                            _navigateToDashboard.value = true
                        } else {
                            _errorMessage.value = "Payment failed. Please try again."
                        }
                    } ?: run {
                        _errorMessage.value = "Verification failed: No data returned"
                    }
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    _errorMessage.value = result.message
                }
                is Resource.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }
}