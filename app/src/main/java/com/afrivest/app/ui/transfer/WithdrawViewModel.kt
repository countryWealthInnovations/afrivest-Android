package com.afrivest.app.ui.transfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.model.WithdrawResponse
import com.afrivest.app.data.repository.WithdrawRepository
import com.afrivest.app.utils.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WithdrawViewModel @Inject constructor(
    private val withdrawRepository: WithdrawRepository
) : ViewModel() {

    private val _withdrawResult = MutableLiveData<Resource<WithdrawResponse>>()
    val withdrawResult: LiveData<Resource<WithdrawResponse>> = _withdrawResult

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

    private fun detectNetwork(phone: String) {
        // Just check the first 2 digits
        when {
            phone.startsWith("77") || phone.startsWith("78") ||
                    phone.startsWith("76") || phone.startsWith("79") -> _selectedNetwork.value = "MTN"

            phone.startsWith("70") || phone.startsWith("74") ||
                    phone.startsWith("75") -> _selectedNetwork.value = "AIRTEL"
        }
    }

    private fun validateForm() {
        val phoneValid = _phoneNumber.value?.length == 9 && _phoneNumber.value?.startsWith("7") == true
        val amountValid = (_amount.value?.toDoubleOrNull() ?: 0.0) >= 10000

        _isFormValid.value = phoneValid && amountValid
    }

    fun initiateWithdraw() {
        viewModelScope.launch {
            _withdrawResult.value = Resource.Loading()

            val formattedPhone = "+256${_phoneNumber.value}"

            val result = withdrawRepository.withdrawMobileMoney(
                amount = _amount.value?.toDouble() ?: 0.0,
                currency = _currency.value ?: "UGX",
                network = _selectedNetwork.value ?: "MTN",
                phoneNumber = formattedPhone
            )

            _withdrawResult.value = result
        }
    }
}