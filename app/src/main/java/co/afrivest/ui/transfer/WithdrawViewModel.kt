package co.afrivest.ui.transfer

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.afrivest.data.local.SecurePreferences
import co.afrivest.data.local.PreferencesManager
import co.afrivest.data.model.Resource
import co.afrivest.data.model.WithdrawResponse
import co.afrivest.data.repository.WithdrawRepository
import co.afrivest.utils.FeeCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PayoutMethod { MOBILE_MONEY, BANK_TRANSFER }

@HiltViewModel
class WithdrawViewModel @Inject constructor(
    private val withdrawRepository: WithdrawRepository,
    private val preferencesManager: PreferencesManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _withdrawResult = MutableLiveData<Resource<WithdrawResponse>>()
    val withdrawResult: LiveData<Resource<WithdrawResponse>> = _withdrawResult

    private val _payoutMethod = MutableLiveData(PayoutMethod.MOBILE_MONEY)
    val payoutMethod: LiveData<PayoutMethod> = _payoutMethod

    private val _selectedNetwork = MutableLiveData("MTN")
    val selectedNetwork: LiveData<String> = _selectedNetwork

    private val _phoneNumber = MutableLiveData("")
    val phoneNumber: LiveData<String> = _phoneNumber

    private val _bankCode = MutableLiveData("")
    val bankCode: LiveData<String> = _bankCode

    private val _accountNumber = MutableLiveData("")
    val accountNumber: LiveData<String> = _accountNumber

    private val _accountName = MutableLiveData("")
    val accountName: LiveData<String> = _accountName

    private val _amount = MutableLiveData("")
    val amount: LiveData<String> = _amount

    private val _selectedCurrency = MutableLiveData("UGX")
    val selectedCurrency: LiveData<String> = _selectedCurrency

    private val _walletCurrency = MutableLiveData(preferencesManager.defaultCurrency ?: "UGX")
    val walletCurrency: LiveData<String> = _walletCurrency

    private val _isFormValid = MutableLiveData(false)
    val isFormValid: LiveData<Boolean> = _isFormValid

    private val _transactionFee = MutableLiveData(0.0)
    val transactionFee: LiveData<Double> = _transactionFee

    private val _totalAmount = MutableLiveData(0.0)
    val totalAmount: LiveData<Double> = _totalAmount

    private val _balanceAfterWithdrawal = MutableLiveData(0.0)
    val balanceAfterWithdrawal: LiveData<Double> = _balanceAfterWithdrawal

    private val _userBalance = MutableLiveData(0.0)
    val userBalance: LiveData<Double> = _userBalance

    private val _insufficientFundsWarning = MutableLiveData(false)
    val insufficientFundsWarning: LiveData<Boolean> = _insufficientFundsWarning

    private val _feeConfirmedByAPI = MutableLiveData(false)
    val feeConfirmedByAPI: LiveData<Boolean> = _feeConfirmedByAPI

    val mobileMoneyNetworks = mapOf(
        "UGX" to listOf("MTN", "AIRTEL"),
        "KES" to listOf("MPESA", "AIRTEL"),
        "NGN" to listOf("MTN", "AIRTEL"),
        "GHS" to listOf("MTN", "VODAFONE", "AIRTEL"),
        "TZS" to listOf("VODACOM", "AIRTEL", "TIGO"),
        "RWF" to listOf("MTN", "AIRTEL"),
        "ZMW" to listOf("MTN", "AIRTEL", "ZAMTEL"),
        "ZAR" to listOf("MTN"),
        "XAF" to listOf("MTN", "AIRTEL"),
        "XOF" to listOf("MTN", "AIRTEL"),
    )

    val bankCurrencies = listOf("UGX","USD","EUR","GBP","KES","NGN","ZAR","CAD","AED","GHS","TZS","RWF")

    fun getAvailableNetworks(): List<String> =
        mobileMoneyNetworks[_selectedCurrency.value] ?: listOf("MTN", "AIRTEL")

    fun setPayoutMethod(method: PayoutMethod) {
        _payoutMethod.value = method
        val nets = getAvailableNetworks()
        if (_selectedNetwork.value !in nets) _selectedNetwork.value = nets.firstOrNull() ?: "MTN"
        validateForm()
    }

    fun setSelectedCurrency(currency: String) {
        _selectedCurrency.value = currency
        val nets = getAvailableNetworks()
        if (_selectedNetwork.value !in nets) _selectedNetwork.value = nets.firstOrNull() ?: "MTN"
        recalculateFee()
        validateForm()
    }

    fun setNetwork(network: String) {
        _selectedNetwork.value = network
        validateForm()
    }

    fun setPhoneNumber(phone: String) {
        _phoneNumber.value = phone
        if (_selectedCurrency.value == "UGX") detectNetwork(phone)
        validateForm()
    }

    fun setBankCode(v: String) { _bankCode.value = v; validateForm() }
    fun setAccountNumber(v: String) { _accountNumber.value = v; validateForm() }
    fun setAccountName(v: String) { _accountName.value = v; validateForm() }

    fun setAmount(amt: String) {
        _amount.value = amt
        recalculateFee()
        validateForm()
    }

    private fun detectNetwork(phone: String) {
        when {
            phone.startsWith("77") || phone.startsWith("78") ||
            phone.startsWith("76") || phone.startsWith("79") -> _selectedNetwork.value = "MTN"
            phone.startsWith("70") || phone.startsWith("74") ||
            phone.startsWith("75") -> _selectedNetwork.value = "AIRTEL"
        }
    }

    private fun validateForm() {
        val amountValid = (_amount.value?.toDoubleOrNull() ?: 0.0) >= 1000
        val sufficientFunds = _insufficientFundsWarning.value != true
        val payoutFieldsValid = if (_payoutMethod.value == PayoutMethod.MOBILE_MONEY) {
            (_phoneNumber.value?.length ?: 0) >= 7
        } else {
            !_bankCode.value.isNullOrBlank() && !_accountNumber.value.isNullOrBlank() && !_accountName.value.isNullOrBlank()
        }
        _isFormValid.value = amountValid && sufficientFunds && payoutFieldsValid
    }

    private fun recalculateFee() {
        val amount = _amount.value?.toDoubleOrNull() ?: return
        if (amount < 1000) {
            _transactionFee.value = 0.0
            _totalAmount.value = 0.0
            _insufficientFundsWarning.value = false
            return
        }
        val currency = _selectedCurrency.value ?: "UGX"
        val method = if (_payoutMethod.value == PayoutMethod.BANK_TRANSFER) "bank_transfer" else "mobile_money"
        val flwFee = FeeCalculator.flutterwavePayoutFee(amount, currency, method)
        val afrivestFee = amount * 0.005
        val fee = flwFee + afrivestFee
        _transactionFee.value = fee
        _totalAmount.value = amount + fee
        _feeConfirmedByAPI.value = false

        val walletCur = _walletCurrency.value ?: "UGX"
        val totalInWallet = if (currency == walletCur) amount + fee
            else FeeCalculator.convertCurrency(amount + fee, from = currency, to = walletCur, preferencesManager = preferencesManager)

        val profile = SecurePreferences(context).getCachedProfile()
        profile?.wallets?.firstOrNull { it.currency == walletCur }?.let { wallet ->
            val balance = wallet.balance.toDoubleOrNull() ?: 0.0
            _userBalance.value = balance
            _balanceAfterWithdrawal.value = balance - totalInWallet
            _insufficientFundsWarning.value = totalInWallet > balance
            validateForm()
        }
    }

    fun initiateWithdraw() {
        viewModelScope.launch {
            _withdrawResult.value = Resource.Loading()
            val amount = _amount.value?.toDouble() ?: 0.0
            val currency = _selectedCurrency.value ?: "UGX"
            val walletCurrency = _walletCurrency.value ?: "UGX"

            val result = if (_payoutMethod.value == PayoutMethod.MOBILE_MONEY) {
                withdrawRepository.withdrawMobileMoney(
                    amount = amount,
                    currency = currency,
                    walletCurrency = walletCurrency,
                    network = _selectedNetwork.value ?: "MTN",
                    phoneNumber = _phoneNumber.value ?: ""
                )
            } else {
                withdrawRepository.withdrawBankTransfer(
                    amount = amount,
                    currency = currency,
                    walletCurrency = walletCurrency,
                    bankCode = _bankCode.value ?: "",
                    accountNumber = _accountNumber.value ?: "",
                    accountName = _accountName.value ?: ""
                )
            }

            if (result is Resource.Success) {
                result.data?.let { response ->
                    response.total_fee?.let { _transactionFee.value = it }
                    response.total_debited?.let { td ->
                        _totalAmount.value = td
                        _feeConfirmedByAPI.value = true
                        val bal = _userBalance.value ?: 0.0
                        if (bal > 0) _balanceAfterWithdrawal.value = bal - td
                    }
                }
            }
            _withdrawResult.value = result
        }
    }
}