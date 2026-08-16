package co.afrivest.ui.deposit

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import co.afrivest.R
import co.afrivest.data.model.Resource
import co.afrivest.databinding.ActivityDepositBinding
import co.afrivest.ui.base.BaseActivity
import co.afrivest.utils.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DepositActivity : BaseActivity() {

    private lateinit var binding: ActivityDepositBinding
    private val viewModel: DepositViewModel by viewModels()
    private var selectedPaymentMethod = "mobile_money" // mobile_money, card, bank_transfer, pay_with_bank

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepositBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupObservers()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Deposit Money"
    }

    private fun setupObservers() {

        // Observe form validity
        viewModel.isFormValid.observe(this) { isValid ->
            updateDepositButtonState(isValid)
        }

        // Observe deposit result
        viewModel.depositResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visible()
                    binding.btnDeposit.disable()
                    binding.textError.gone()
                }

                is Resource.Success -> {
                    binding.progressBar.gone()
                    resource.data?.let { depositResponse ->
                        val paymentUrl = depositResponse.payment_data.paymentUrl
                            ?: depositResponse.payment_data.redirect_url

                        if (paymentUrl != null) {
                            // Open WebView for redirect-based payments
                            val intent = Intent(this, DepositWebViewActivity::class.java).apply {
                                putExtra("TRANSACTION_ID", depositResponse.transaction_id)
                                putExtra("REFERENCE", depositResponse.reference)
                                putExtra("PAYMENT_URL", paymentUrl)
                                putExtra("AMOUNT", depositResponse.amount)
                                putExtra("CURRENCY", depositResponse.currency)
                                putExtra("NETWORK", depositResponse.network)
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            // Push notification flow (Mpesa, MTN Uganda etc)
                            // Show success message and go back
                            android.app.AlertDialog.Builder(this)
                                .setTitle("Payment Initiated")
                                .setMessage("Please approve the payment on your phone when prompted. Reference: ${depositResponse.reference}")
                                .setPositiveButton("OK") { _, _ -> finish() }
                                .show()
                        }
                    } ?: run {
                        binding.btnDeposit.enable()
                        binding.textError.visible()
                        binding.textError.text = "Invalid response from server"
                    }
                }

                is Resource.Error -> {
                    binding.progressBar.gone()
                    binding.btnDeposit.enable()
                    binding.textError.visible()
                    binding.textError.text = resource.message
                }
            }
        }

        viewModel.bankDepositResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visible()
                    binding.btnDeposit.disable()
                    binding.textError.gone()
                }
                is Resource.Success -> {
                    binding.progressBar.gone()
                    resource.data?.let { response ->
                        val redirectUrl = response.payment_data?.redirect_url
                            ?: response.payment_data?.authorization_url

                        val pd = response.payment_data
                        val bankType = viewModel.selectedBankType.value
                        val isRedirectFlow = bankType == "pay_with_bank_ng" || bankType == "pay_with_bank_uk"

                        if (redirectUrl != null && isRedirectFlow) {
                            // NG/UK/EU Pay With Bank — redirect only, no confirmation screen needed
                            val intent = Intent(this, DepositWebViewActivity::class.java).apply {
                                putExtra("TRANSACTION_ID", response.transaction_id)
                                putExtra("REFERENCE", response.reference)
                                putExtra("PAYMENT_URL", redirectUrl)
                                putExtra("AMOUNT", response.amount.toString())
                                putExtra("CURRENCY", response.currency)
                                putExtra("NETWORK", "BANK")
                            }
                            startActivity(intent)
                            finish()
                        } else if (pd?.transfer_account != null && pd.transfer_bank != null) {
                            // NGN Bank Transfer — confirmation screen
                            val intent = Intent(this, BankTransferConfirmationActivity::class.java).apply {
                                putExtra("TRANSACTION_ID", response.transaction_id)
                                putExtra("TYPE", "bank_transfer")
                                putExtra("BANK_NAME", pd.transfer_bank)
                                putExtra("ACCOUNT_NUMBER", pd.transfer_account)
                                putExtra("AMOUNT", (pd.transfer_amount ?: response.amount).toString())
                                putExtra("CURRENCY", response.currency)
                                putExtra("REFERENCE", pd.transfer_note ?: response.reference)
                                putExtra("EXPIRATION", pd.account_expiration)
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            binding.btnDeposit.enable()
                            binding.textError.visible()
                            binding.textError.text = "Unable to process bank deposit. Please try again."
                        }
                    } ?: run {
                        binding.btnDeposit.enable()
                        binding.textError.visible()
                        binding.textError.text = "Invalid response from server"
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.gone()
                    binding.btnDeposit.enable()
                    android.app.AlertDialog.Builder(this)
                        .setTitle("Deposit Failed")
                        .setMessage(resource.message)
                        .setPositiveButton("OK", null)
                        .show()
                }
                else -> {}
            }
        }
    }

    private fun setupListeners() {
        // Payment Method Toggle
        binding.togglePaymentMethod.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnMobileMoney -> {
                        selectedPaymentMethod = "mobile_money"
                        binding.editTextAmount.setText("")
                        binding.layoutMobileMoney.visible()
                        binding.layoutCard.gone()
                        binding.layoutBank.gone()
                        updateAmountHint()
                        updateDepositButtonState(viewModel.isFormValid.value ?: false)
                    }
                    // Card deposit temporarily disabled
                    // R.id.btnCard -> {
                    //     selectedPaymentMethod = "card"
                    //     binding.layoutMobileMoney.gone()
                    //     binding.layoutCard.visible()
                    //     binding.layoutBank.gone()
                    //     binding.editTextAmount.setText("")
                    //     updateCardCurrencyChips()
                    // }

                    R.id.btnBank -> {
                        selectedPaymentMethod = "bank"
                        binding.editTextAmount.setText("")
                        binding.layoutMobileMoney.gone()
                        binding.layoutCard.gone()
                        binding.layoutBank.visible()
                        viewModel.setBankSubMethod("virtual_account")
                        binding.toggleBankSubMethod.check(R.id.btnVirtualAccount)
                        updateBankCurrencyChips()
                        binding.tvAmountLabel.text = "Amount (${viewModel.selectedBankCurrency.value ?: "NGN"})"
                        updateAmountHint()
                        updateDepositButtonState(viewModel.isFormValid.value ?: false)
                    }
                }
            }
        }

        // Card Number Formatting
        binding.editTextCardNumber.doOnTextChanged { text, _, _, _ ->
            val cleanText = text.toString().replace(" ", "")
            if (cleanText.length <= 16) {
                val formatted = cleanText.chunked(4).joinToString(" ")
                if (formatted != text.toString()) {
                    binding.editTextCardNumber.setText(formatted)
                    binding.editTextCardNumber.setSelection(formatted.length)
                }
            }
        }

        // Card Fields
        binding.editTextExpiryMonth.doOnTextChanged { text, _, _, _ ->
            if (text.toString().length >= 2) {
                binding.editTextExpiryYear.requestFocus()
            }
        }

        binding.editTextExpiryYear.doOnTextChanged { text, _, _, _ ->
            if (text.toString().length >= 2) {
                binding.editTextCVV.requestFocus()
            }
        }

        // Amount input
        binding.editTextAmount.doOnTextChanged { text, _, _, _ ->
            val amt = text?.toString() ?: ""
            viewModel.setAmount(amt)
            val amountVal = amt.toDoubleOrNull() ?: 0.0
            if (amountVal > 0) {
                binding.feeSection.visible()
                val currency = if (selectedPaymentMethod == "bank") {
                    viewModel.selectedBankCurrency.value ?: "NGN"
                } else {
                    viewModel.currency.value ?: "UGX"
                }
                val method = if (selectedPaymentMethod == "card") "card" else "mobile_money"
                val fee = FeeCalculator.flutterwaveCollectionFee(amountVal, currency, method)
                val total = amountVal + fee
                val minAmount = when (selectedPaymentMethod) {
                    "card" -> viewModel.getCardMinimumAmount()
                    "bank" -> viewModel.getBankMinimumAmount()
                    else -> viewModel.getMinimumAmount()
                }
                binding.tvDepositAmount.text = "$currency ${FeeCalculator.formatCurrency(amountVal)}"
                binding.tvDepositFeeAmount.text = if (fee == 0.0) "Free" else "$currency ${FeeCalculator.formatCurrency(fee)}"
                binding.tvDepositTotal.text = "$currency ${FeeCalculator.formatCurrency(total)}"
                if (amountVal < minAmount) {
                    binding.textError.visible()
                    binding.textError.text = "Minimum amount is $currency ${FeeCalculator.formatCurrency(minAmount)}"
                } else {
                    binding.textError.gone()
                }
            } else {
                binding.feeSection.gone()
                binding.textError.gone()
            }
            updateDepositButtonState(viewModel.isFormValid.value ?: false)
        }

        // Currency chips
        updateCurrencyChips()
        updateNetworkChips()
        updateAmountHint()
        setupBankSubMethodToggle()

        // Observe currency changes
        viewModel.currency.observe(this) { currency ->
            if (selectedPaymentMethod == "mobile_money" || selectedPaymentMethod == "card") {
                binding.tvAmountLabel.text = "Amount ($currency)"
            }
            binding.tvCountryCode.text = viewModel.getDialCode()
            updateNetworkChips()
            updateCurrencyChips()
            updateAmountHint()
        }

        // Observe bank currency changes
        viewModel.selectedBankCurrency.observe(this) { currency ->
            if (selectedPaymentMethod == "bank") {
                binding.tvAmountLabel.text = "Amount ($currency)"
            }
            updateAmountHint()
        }

        // Phone number input with validation
        binding.editTextPhone.doOnTextChanged { text, _, _, _ ->
            val phone = text?.toString() ?: ""
            viewModel.setPhoneNumber(phone)
            val minLength = when (viewModel.currency.value) {
                "UGX" -> 9
                "KES" -> 9
                "NGN" -> 10
                "GHS" -> 9
                "TZS" -> 9
                "RWF" -> 9
                "ZMW" -> 9
                "XAF", "XOF" -> 9
                else -> 9
            }
            binding.tvPhoneHelperDeposit.text = when {
                phone.length >= minLength -> "Valid ✓"
                phone.isNotEmpty() -> "Enter full phone number"
                else -> "Enter phone number"
            }
            binding.tvPhoneHelperDeposit.setTextColor(
                if (phone.length >= minLength) getColor(R.color.success_green)
                else getColor(R.color.text_secondary)
            )
        }

        // Deposit button
        binding.btnDeposit.setOnClickListener {
            when (selectedPaymentMethod) {
                "mobile_money" -> if (viewModel.isFormValid.value == true) viewModel.initiateDeposit()
                "card" -> initiateCardDeposit()
                "bank" -> initiateBankDeposit()
            }
        }
    }


    private fun updateDepositButtonState(mobileMoneyFormValid: Boolean) {
        val amountVal = binding.editTextAmount.text.toString().toDoubleOrNull() ?: 0.0

        val shouldEnable = when (selectedPaymentMethod) {
            "mobile_money" -> mobileMoneyFormValid

            "card" -> binding.editTextAmount.text.toString().isNotEmpty() &&
                    binding.editTextCardNumber.text.toString().replace(" ", "").length == 16 &&
                    binding.editTextExpiryMonth.text.toString().length == 2 &&
                    binding.editTextExpiryYear.text.toString().length == 2 &&
                    binding.editTextCVV.text.toString().length == 3

            "bank" -> amountVal >= viewModel.getBankMinimumAmount()

            else -> false
        }

        if (shouldEnable) {
            binding.btnDeposit.enable()
        } else {
            binding.btnDeposit.disable()
        }
    }

    private fun initiateCardDeposit() {
        val amount = binding.editTextAmount.text.toString()
        val cardNumber = binding.editTextCardNumber.text.toString().replace(" ", "")
        val expiryMonth = binding.editTextExpiryMonth.text.toString()
        val expiryYear = binding.editTextExpiryYear.text.toString()
        val cvv = binding.editTextCVV.text.toString()

        // Validate
        val minAmount = viewModel.getCardMinimumAmount()
        val currency = viewModel.currency.value ?: "UGX"
        if (amount.isEmpty() || amount.toDoubleOrNull() == null || amount.toDouble() < minAmount) {
            binding.textError.visible()
            binding.textError.text = "Please enter a valid amount (min $currency ${FeeCalculator.formatCurrency(minAmount)})"
            return
        }

        if (cardNumber.length != 16) {
            binding.textError.visible()
            binding.textError.text = "Please enter a valid 16-digit card number"
            return
        }

        if (expiryMonth.length != 2 || expiryYear.length != 2) {
            binding.textError.visible()
            binding.textError.text = "Please enter valid expiry date"
            return
        }

        if (cvv.length != 3) {
            binding.textError.visible()
            binding.textError.text = "Please enter a valid 3-digit CVV"
            return
        }

        viewModel.initiateCardDeposit(
            amount = amount.toDouble(),
            cardNumber = cardNumber,
            expiryMonth = expiryMonth,
            expiryYear = expiryYear,
            cvv = cvv
        )
    }

    private fun updateCardCurrencyChips() {
        binding.chipGroupCardCurrency.removeAllViews()
        viewModel.cardCurrencies.forEach { currency ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = currency
                isCheckable = true
                isChecked = currency == viewModel.currency.value
                setOnClickListener { viewModel.setSelectedCurrency(currency) }
            }
            binding.chipGroupCardCurrency.addView(chip)
        }
    }

    private fun updateCurrencyChips() {
        binding.chipGroupDepositCurrency.removeAllViews()
        viewModel.getAvailableCurrencies().forEach { currency ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = currency
                isCheckable = true
                isChecked = currency == viewModel.currency.value
                setOnClickListener { viewModel.setSelectedCurrency(currency) }
            }
            binding.chipGroupDepositCurrency.addView(chip)
        }
    }

    private fun updateNetworkChips() {
        binding.chipGroupDepositNetwork.removeAllViews()
        val networks = viewModel.getAvailableNetworks()
        // Auto-select first network when currency changes
        if (viewModel.selectedNetwork.value !in networks) {
            networks.firstOrNull()?.let { viewModel.setNetwork(it) }
        }
        networks.forEach { network ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = network
                isCheckable = true
                isChecked = network == viewModel.selectedNetwork.value
                setOnClickListener { viewModel.setNetwork(network) }
            }
            binding.chipGroupDepositNetwork.addView(chip)
        }
    }

    private fun setupBankSubMethodToggle() {
        binding.toggleBankSubMethod.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnVirtualAccount -> {
                        binding.editTextAmount.setText("")
                        viewModel.setBankSubMethod("virtual_account")
                        binding.layoutVirtualAccountOptions.visible()
                        binding.layoutInternetBankingOptions.gone()
                        updateBankCurrencyChips()
                        binding.tvBankInfo.text = "A virtual bank account will be generated for you to transfer into."
                    }
                    R.id.btnInternetBanking -> {
                        binding.editTextAmount.setText("")
                        viewModel.setBankSubMethod("internet_banking")
                        binding.layoutVirtualAccountOptions.gone()
                        binding.layoutInternetBankingOptions.visible()
                        updateBankCountryChips()
                        binding.tvBankInfo.text = "You will be redirected to your bank to complete this payment."
                    }
                }
                binding.tvAmountLabel.text = "Amount (${viewModel.selectedBankCurrency.value ?: "NGN"})"
                updateAmountHint()
                updateDepositButtonState(viewModel.isFormValid.value ?: false)
            }
        }
    }

    private fun updateBankCurrencyChips() {
        binding.chipGroupBankCurrency.removeAllViews()
        listOf("NGN", "GHS").forEach { currency ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = currency
                isCheckable = true
                isChecked = currency == viewModel.selectedBankCurrency.value
                setOnClickListener {
                    binding.editTextAmount.setText("")
                    viewModel.setSelectedBankCurrency(currency)
                    viewModel.setSelectedBankType("bank_transfer")
                    binding.tvAmountLabel.text = "Amount ($currency)"
                    updateAmountHint()
                    updateDepositButtonState(viewModel.isFormValid.value ?: false)
                }
            }
            binding.chipGroupBankCurrency.addView(chip)
        }
    }

    private fun updateBankCountryChips() {
        binding.chipGroupBankCountry.removeAllViews()
        listOf("NG", "UK", "EU").forEach { country ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = country
                isCheckable = true
                isChecked = country == (viewModel.payWithBankCountry.value ?: "NG")
                setOnClickListener {
                    binding.editTextAmount.setText("")
                    viewModel.setPayWithBankCountry(country)
                    binding.tvAmountLabel.text = "Amount (${viewModel.selectedBankCurrency.value ?: "NGN"})"
                    updateAmountHint()
                    updateDepositButtonState(viewModel.isFormValid.value ?: false)
                }
            }
            binding.chipGroupBankCountry.addView(chip)
        }
    }

    private fun updateAmountHint() {
        val (currency, minAmount) = when (selectedPaymentMethod) {
            "bank" -> Pair(viewModel.selectedBankCurrency.value ?: "NGN", viewModel.getBankMinimumAmount())
            "card" -> Pair(viewModel.currency.value ?: "UGX", viewModel.getCardMinimumAmount())
            else -> Pair(viewModel.currency.value ?: "UGX", viewModel.getMinimumAmount())
        }
        binding.tvAmountMinHint.text = "Minimum: $currency ${FeeCalculator.formatCurrency(minAmount)}"
    }

    private fun initiateBankDeposit() {
        val amount = binding.editTextAmount.text.toString()
        if (amount.isEmpty() || amount.toDoubleOrNull() == null || amount.toDouble() < 1) {
            binding.textError.visible()
            binding.textError.text = "Please enter a valid amount"
            return
        }
        val type = viewModel.selectedBankType.value ?: "bank_transfer"
        BankTransferConfirmationActivity.let {
            getSharedPreferences(BankTransferConfirmationActivity.PREFS_NAME, MODE_PRIVATE)
                .edit().remove(BankTransferConfirmationActivity.KEY_PENDING_TRANSFER).apply()
        }
        viewModel.initiateBankDeposit(amount.toDouble())
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}