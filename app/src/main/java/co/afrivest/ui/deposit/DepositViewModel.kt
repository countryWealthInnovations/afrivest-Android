package co.afrivest.ui.deposit

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.afrivest.data.api.DepositResponse
import co.afrivest.data.api.TransactionStatus
import co.afrivest.data.api.CardDepositRequest
import co.afrivest.data.api.BankDepositApiResponse
import co.afrivest.data.model.Resource
import co.afrivest.data.repository.DepositRepository
import co.afrivest.utils.Validators
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

    private val _bankDepositResult = MutableLiveData<Resource<BankDepositApiResponse>>()
    val bankDepositResult: LiveData<Resource<BankDepositApiResponse>> = _bankDepositResult

    private val _selectedBankCurrency = MutableLiveData<String>("NGN")
    val selectedBankCurrency: LiveData<String> = _selectedBankCurrency

    private val _selectedBankType = MutableLiveData<String>("bank_transfer")
    val selectedBankType: LiveData<String> = _selectedBankType

    private val _payWithBankCountry = MutableLiveData<String>("NG")
    val payWithBankCountry: LiveData<String> = _payWithBankCountry

    private val _bankSubMethod = MutableLiveData<String>("virtual_account")
    val bankSubMethod: LiveData<String> = _bankSubMethod

    fun setSelectedBankCurrency(currency: String) {
        _selectedBankCurrency.value = currency
    }

    fun setSelectedBankType(type: String) {
        _selectedBankType.value = type
    }

    fun setBankSubMethod(method: String) {
        _bankSubMethod.value = method
        if (method == "virtual_account") {
            _selectedBankCurrency.value = "NGN"
            _selectedBankType.value = "bank_transfer"
        } else {
            setPayWithBankCountry("NG")
        }
    }

    fun setPayWithBankCountry(country: String) {
        _payWithBankCountry.value = country
        when (country) {
            "UK" -> {
                _selectedBankCurrency.value = "GBP"
                _selectedBankType.value = "pay_with_bank_uk"
            }
            "EU" -> {
                _selectedBankCurrency.value = "EUR"
                _selectedBankType.value = "pay_with_bank_uk"
            }
            else -> {
                _selectedBankCurrency.value = "NGN"
                _selectedBankType.value = "pay_with_bank_ng"
            }
        }
    }

    fun initiateBankDeposit(amount: Double) {
        viewModelScope.launch {
            _bankDepositResult.value = Resource.Loading()
            val result = depositRepository.depositBank(
                amount = amount,
                currency = _selectedBankCurrency.value ?: "NGN",
                type = _selectedBankType.value ?: "bank_transfer"
            )
            _bankDepositResult.value = result
        }
    }

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

    val mobileMoneyNetworks = mapOf(
        "UGX" to listOf("MTN", "AIRTEL"),
        "KES" to listOf("MPESA"),
        "NGN" to listOf("ENAIRA"),
        "GHS" to listOf("MTN", "TELECEL", "AIRTEL"),
        "TZS" to listOf("AIRTEL", "TIGO", "HALOPESA"),
        "RWF" to listOf("AIRTEL", "MTN"),
        "ZMW" to listOf("AIRTEL", "MTN", "ZAMTEL"),
        "XAF" to listOf("MTN", "ORANGE"),
        "XOF" to listOf("MTN", "ORANGE", "WAVE"),
    )

    val cardCurrencies = listOf("UGX", "USD", "EUR", "GBP", "KES", "NGN", "ZAR", "CAD", "AED")

    fun getCardMinimumAmount(): Double = when (_currency.value) {
        "UGX" -> 5000.0
        "USD", "GBP", "EUR", "CAD", "AED" -> 1.0
        "KES" -> 50.0
        "NGN" -> 500.0
        "ZAR" -> 5.0
        else -> 1.0
    }

    fun getAvailableNetworks(): List<String> =
        mobileMoneyNetworks[_currency.value] ?: listOf("MTN", "AIRTEL")

    fun getAvailableCurrencies(): List<String> =
        mobileMoneyNetworks.keys.sorted()

    fun setSelectedCurrency(curr: String) {
        _currency.value = curr
        val nets = getAvailableNetworks()
        if (_selectedNetwork.value !in nets) _selectedNetwork.value = nets.firstOrNull() ?: "MTN"
        validateForm()
    }

    fun getDialCode(): String = when (_currency.value) {
        "UGX" -> "+256"
        "KES" -> "+254"
        "NGN" -> "+234"
        "GHS" -> "+233"
        "TZS" -> "+255"
        "RWF" -> "+250"
        "ZMW" -> "+260"
        "XAF" -> "+237"
        "XOF" -> "+225"
        else -> "+256"
    }

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
        if (_currency.value != "UGX") return
        when {
            phone.startsWith("77") || phone.startsWith("78") ||
                    phone.startsWith("76") || phone.startsWith("79") -> setNetwork("MTN")
            phone.startsWith("70") || phone.startsWith("74") ||
                    phone.startsWith("75") -> setNetwork("AIRTEL")
        }
    }

    fun getBankMinimumAmount(): Double = when (_selectedBankCurrency.value) {
        "NGN" -> 100.0
        "GHS" -> 5.0
        "GBP", "EUR" -> 1.0
        else -> 1.0
    }

    fun getMinimumAmount(): Double = when (_currency.value) {
        "UGX" -> 5000.0
        "KES" -> 50.0
        "NGN" -> 100.0
        "GHS" -> 5.0
        "TZS" -> 10000.0
        "RWF" -> 1000.0
        "ZMW" -> 10.0
        "XAF", "XOF" -> 500.0
        else -> 5000.0
    }

    private fun validateForm() {
        val phoneValid = Validators.isValidPhoneNumber(_phoneNumber.value ?: "")
        val amountValid = (_amount.value?.toDoubleOrNull() ?: 0.0) >= getMinimumAmount()
        val networkValid = _selectedNetwork.value in getAvailableNetworks()

        _isFormValid.value = phoneValid && amountValid && networkValid
    }

    fun initiateDeposit() {
        viewModelScope.launch {
            _depositResult.value = Resource.Loading()
            val dialCode = getDialCode()
            val rawPhone = _phoneNumber.value ?: ""
            val formattedPhone = if (rawPhone.startsWith("+")) rawPhone else "$dialCode$rawPhone"

            val result = depositRepository.depositMobileMoney(
                amount = _amount.value?.toDouble() ?: 0.0,
                currency = _currency.value ?: "UGX",
                network = _selectedNetwork.value ?: "MTN",
                phoneNumber = formattedPhone
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