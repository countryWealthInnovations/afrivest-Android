package com.afrivest.app.ui.deposit

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.afrivest.app.R
import com.afrivest.app.data.model.Resource
import com.afrivest.app.databinding.ActivityDepositBinding
import com.afrivest.app.utils.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DepositActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDepositBinding
    private val viewModel: DepositViewModel by viewModels()
    private var selectedPaymentMethod = "mobile_money" // or "card"

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
            // Enable button if either mobile money form is valid OR card fields are filled
            val shouldEnable = if (selectedPaymentMethod == "mobile_money") {
                isValid
            } else {
                // For card, check if all fields are filled
                binding.editTextAmount.text.toString().isNotEmpty() &&
                        binding.editTextCardNumber.text.toString().replace(" ", "").length == 16 &&
                        binding.editTextExpiryMonth.text.toString().length == 2 &&
                        binding.editTextExpiryYear.text.toString().length == 2 &&
                        binding.editTextCVV.text.toString().length == 3
            }

            if (shouldEnable) {
                binding.btnDeposit.enable()
            } else {
                binding.btnDeposit.disable()
            }
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
                        // Open WebView with Flutterwave payment page
                        val intent = Intent(this, DepositWebViewActivity::class.java).apply {
                            putExtra("TRANSACTION_ID", depositResponse.transaction_id)
                            putExtra("REFERENCE", depositResponse.reference)
                            putExtra("PAYMENT_URL", depositResponse.payment_data.paymentUrl
                                ?: depositResponse.payment_data.redirect_url)
                            putExtra("AMOUNT", depositResponse.amount)
                            putExtra("CURRENCY", depositResponse.currency)
                            putExtra("NETWORK", depositResponse.network)
                        }
                        startActivity(intent)
                        finish()
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
    }

    private fun setupListeners() {
        // Payment Method Toggle
        binding.togglePaymentMethod.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnMobileMoney -> {
                        selectedPaymentMethod = "mobile_money"
                        binding.layoutMobileMoney.visible()
                        binding.layoutCard.gone()
                    }
                    R.id.btnCard -> {
                        selectedPaymentMethod = "card"
                        binding.layoutMobileMoney.gone()
                        binding.layoutCard.visible()
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
            viewModel.setAmount(text?.toString() ?: "")
        }

        // Phone number input with validation
        binding.editTextPhone.doOnTextChanged { text, _, _, _ ->
            val phone = text?.toString() ?: ""
            viewModel.setPhoneNumber(phone)

            // Show detected network
            when {
                phone.startsWith("77") || phone.startsWith("78") ||
                        phone.startsWith("76") || phone.startsWith("79") -> {
                    binding.tvPhoneHelperDeposit.text = "MTN detected ✓"
                    binding.tvPhoneHelperDeposit.setTextColor(getColor(R.color.success_green))
                }
                phone.startsWith("70") || phone.startsWith("74") ||
                        phone.startsWith("75") -> {
                    binding.tvPhoneHelperDeposit.text = "Airtel detected ✓"
                    binding.tvPhoneHelperDeposit.setTextColor(getColor(R.color.success_green))
                }
                else -> {
                    binding.tvPhoneHelperDeposit.text = "MTN: 77, 78, 76, 79 | Airtel: 70, 74, 75"
                    binding.tvPhoneHelperDeposit.setTextColor(getColor(R.color.text_secondary))
                }
            }

            // Validate phone length (9 digits for Uganda)
            if (phone.length == 9) {
                binding.tvPhoneHelperDeposit.text = "${binding.tvPhoneHelperDeposit.text} - Valid ✓"
                binding.tvPhoneHelperDeposit.setTextColor(getColor(R.color.success_green))
            } else if (phone.length > 9) {
                binding.tvPhoneHelperDeposit.text = "Phone number too long"
                binding.tvPhoneHelperDeposit.setTextColor(getColor(R.color.error_red))
            }
        }

        // Deposit button
        binding.btnDeposit.setOnClickListener {
            if (selectedPaymentMethod == "mobile_money") {
                if (viewModel.isFormValid.value == true) {
                    viewModel.initiateDeposit()
                }
            } else {
                initiateCardDeposit()
            }
        }
    }


    private fun initiateCardDeposit() {
        val amount = binding.editTextAmount.text.toString()
        val cardNumber = binding.editTextCardNumber.text.toString().replace(" ", "")
        val expiryMonth = binding.editTextExpiryMonth.text.toString()
        val expiryYear = binding.editTextExpiryYear.text.toString()
        val cvv = binding.editTextCVV.text.toString()

        // Validate
        if (amount.isEmpty() || amount.toDoubleOrNull() == null || amount.toDouble() < 1000) {
            binding.textError.visible()
            binding.textError.text = "Please enter a valid amount (min 1,000)"
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}