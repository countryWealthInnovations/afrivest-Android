package com.afrivest.app.ui.deposit

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afrivest.app.data.api.DepositResponse
import com.afrivest.app.data.api.TransactionStatus
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.repository.DepositRepository
import com.afrivest.app.utils.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DepositViewModel @Inject constructor(
    private val depositRepository: DepositRepository
) : ViewModel() {

    private val _depositResult = MutableLiveData<Resource<DepositResponse>>()
    val depositResult: LiveData<Resource<DepositResponse>> = _depositResult

    private val _statusResult = MutableLiveData<Resource<TransactionStatus>>()
    val statusResult: LiveData<Resource<TransactionStatus>> = _statusResult

    private val _selectedNetwork = MutableLiveData<String>("MTN")
    val selectedNetwork: LiveData<String> = _selectedNetwork

    private val _phoneNumber = MutableLiveData<String>("")
    val phoneNumber: LiveData<String> = _phoneNumber

    private val _amount = MutableLiveData<String>("")
    val amount: LiveData<String> = _amount

    private val _currency = MutableLiveData<String>("UGX")
    val currency: LiveData<String> = _currency

    private val _isFormValid = MutableLiveData<Boolean>(false)
    val isFormValid: LiveData<Boolean> = _isFormValid

    fun setNetwork(network: String) {
        _selectedNetwork.value = network
        validateForm()
    }

    fun setPhoneNumber(phone: String) {
        _phoneNumber.value = phone
        validateForm()

        // Auto-detect network
        if (phone.isNotEmpty()) {
            detectNetwork(phone)
        }
    }

    fun setAmount(amt: String) {
        _amount.value = amt
        validateForm()
    }

    fun setCurrency(curr: String) {
        _currency.value = curr
    }

    private fun detectNetwork(phone: String) {
        // Just check the first 2 digits
        when {
            phone.startsWith("77") || phone.startsWith("78") ||
                    phone.startsWith("76") || phone.startsWith("79") -> setNetwork("MTN")

            phone.startsWith("70") || phone.startsWith("74") ||
                    phone.startsWith("75") -> setNetwork("AIRTEL")
        }
    }

    private fun validateForm() {
        val phoneValid = Validators.isValidPhoneNumber(_phoneNumber.value ?: "")
        val amountValid = (_amount.value?.toDoubleOrNull() ?: 0.0) >= 4999
        val networkValid = _selectedNetwork.value in listOf("MTN", "AIRTEL")

        _isFormValid.value = phoneValid && amountValid && networkValid
    }

    fun initiateDeposit() {
        viewModelScope.launch {
            _depositResult.value = Resource.Loading()

            val result = depositRepository.depositMobileMoney(
                amount = _amount.value?.toDouble() ?: 0.0,
                currency = _currency.value ?: "UGX",
                network = _selectedNetwork.value ?: "MTN",
                phoneNumber = Validators.formatPhoneNumber(_phoneNumber.value ?: "")
            )

            _depositResult.value = result
        }
    }

    fun initiateCardDeposit(
        amount: Double,
        cardNumber: String,
        expiryMonth: String,
        expiryYear: String,
        cvv: String
    ) {
        viewModelScope.launch {
            _depositResult.value = Resource.Loading()

            val result = depositRepository.depositCard(
                amount = amount,
                currency = _currency.value ?: "UGX",
                cardNumber = cardNumber,
                cvv = cvv,
                expiryMonth = expiryMonth,
                expiryYear = expiryYear
            )
            _depositResult.value = result
        }
    }

}