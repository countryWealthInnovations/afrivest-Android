package com.afrivest.app.ui.deposit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.afrivest.app.R
import com.afrivest.app.databinding.ActivityDepositBinding
import com.afrivest.app.utils.Validators
import com.afrivest.app.utils.gone
import com.afrivest.app.utils.onTextChanged
import com.afrivest.app.utils.visible
import com.afrivest.app.utils.clearError
import com.afrivest.app.utils.showError
import com.afrivest.app.utils.showSuccess
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DepositActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDepositBinding

    private val currencies = listOf("UGX", "USD", "EUR", "GBP")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepositBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupCurrencySpinner()
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

            // Add your deposit logic here
            initiateDeposit(amount, currency, phoneText)
        }
    }

    private fun initiateDeposit(amount: Double, currency: String, phone: String) {
        // Show loading
        binding.progressBar.visible()
        binding.btnDeposit.isEnabled = false

        // TODO: Implement your deposit initiation logic here
        Log.d("DepositActivity", "Initiating deposit: amount=$amount, currency=$currency, phone=$phone")

        // Hide loading when done
        binding.progressBar.gone()
        binding.btnDeposit.isEnabled = true
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}