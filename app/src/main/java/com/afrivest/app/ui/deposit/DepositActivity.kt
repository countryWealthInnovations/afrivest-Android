package com.afrivest.app.ui.deposit

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.afrivest.app.R
import com.afrivest.app.data.repository.SDKInitiateResponse
import com.afrivest.app.databinding.ActivityDepositBinding
import com.afrivest.app.services.payment.FlutterwaveSDKManager
import com.afrivest.app.utils.Validators
import com.afrivest.app.utils.gone
import com.afrivest.app.utils.onTextChanged
import com.afrivest.app.utils.visible
import com.afrivest.app.utils.clearError
import com.afrivest.app.utils.showError
import com.afrivest.app.utils.showSuccess
import com.afrivest.app.utils.onTextChanged
import com.afrivest.app.utils.visible
import com.afrivest.app.utils.gone

class DepositActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDepositBinding
    private val viewModel: DepositViewModel by viewModels()
    private val flutterwaveSDK = FlutterwaveSDKManager()

    private val currencies = listOf("UGX", "USD", "EUR", "GBP")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepositBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupCurrencySpinner()
        setupObservers()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Deposit Money"
    }

    private fun setupCurrencySpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCurrency.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { loading ->
            if (loading) {
                binding.progressBar.visible()
                binding.btnDeposit.isEnabled = false
            } else {
                binding.progressBar.gone()
                binding.btnDeposit.isEnabled = true
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                binding.textError.visible()
                binding.textError.text = it
                binding.textSuccess.gone()
            }
        }

        viewModel.successMessage.observe(this) { success ->
            success?.let {
                binding.textSuccess.visible()
                binding.textSuccess.text = it
                binding.textError.gone()
            }
        }

        viewModel.navigateToDashboard.observe(this) { shouldNavigate ->
            if (shouldNavigate) {
                finish()
            }
        }
    }

    private fun setupListeners() {
        // Phone number validation
        binding.editTextPhone.onTextChanged { phone ->
            if (phone.length >= 10) {
                if (Validators.isValidPhoneNumber(phone)) {
                    binding.textInputLayoutPhone.clearError()
                    binding.textInputLayoutPhone.showSuccess()
                } else {
                    binding.textInputLayoutPhone.showError("Invalid phone number")
                }
            } else {
                binding.textInputLayoutPhone.clearError()
            }
        }

        binding.btnDeposit.setOnClickListener {
            val amountText = binding.editTextAmount.text.toString()
            val phoneText = binding.editTextPhone.text.toString()

            if (amountText.isEmpty()) {
                binding.textError.visible()
                binding.textError.text = "Please enter amount"
                return@setOnClickListener
            }

            if (phoneText.isEmpty()) {
                binding.textError.visible()
                binding.textError.text = "Please enter phone number"
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull() ?: 0.0
            val currency = binding.spinnerCurrency.selectedItem.toString()

            viewModel.validateAndInitiateDeposit(amount, currency, phoneText)
        }
    }

    private fun launchFlutterwaveSDK(transaction: SDKInitiateResponse) {
        val nameParts = transaction.user.name.split(" ")
        val firstName = nameParts.firstOrNull() ?: transaction.user.name
        val lastName = nameParts.drop(1).joinToString(" ")

        // Use user-entered phone number
        val phoneToUse = Validators.formatPhoneNumber(viewModel.getPhoneNumber())

        flutterwaveSDK.initiatePayment(
            activity = this,
            amount = transaction.amount,
            currency = transaction.currency,
            txRef = transaction.tx_ref,
            email = transaction.user.email,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneToUse, // Use formatted entered phone
            publicKey = transaction.sdk_config.public_key,
            encryptionKey = transaction.sdk_config.encryption_key,
            isStaging = false
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        flutterwaveSDK.handleActivityResult(requestCode, resultCode, data) { result ->
            when (result) {
                is FlutterwaveSDKManager.PaymentResult.Success -> {
                    viewModel.verifyDeposit(result.flwRef, "successful")
                }
                is FlutterwaveSDKManager.PaymentResult.Failed -> {
                    Toast.makeText(this, "Payment failed: ${result.message}", Toast.LENGTH_LONG).show()
                    viewModel.verifyDeposit("", "failed")
                }
                is FlutterwaveSDKManager.PaymentResult.Cancelled -> {
                    Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}