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
        // Observe network selection
        viewModel.selectedNetwork.observe(this) { network ->
            updateNetworkSelection(network)
        }

        // Observe form validity
        viewModel.isFormValid.observe(this) { isValid ->
            binding.btnDeposit.isEnabled = isValid
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
                            putExtra("PAYMENT_URL", depositResponse.payment_data.authorization_url
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
        // Amount input
        binding.editTextAmount.doOnTextChanged { text, _, _, _ ->
            viewModel.setAmount(text?.toString() ?: "")
        }

        // Phone number input with validation
        binding.editTextPhone.doOnTextChanged { text, _, _, _ ->
            val phone = text?.toString() ?: ""
            viewModel.setPhoneNumber(phone)

            if (phone.length >= 10) {
                if (Validators.isValidPhoneNumber(phone)) {
                    binding.textInputLayoutPhone.clearError()
                    binding.textInputLayoutPhone.showSuccess()
                } else {
                    binding.textInputLayoutPhone.showError("Invalid phone number format")
                }
            } else {
                binding.textInputLayoutPhone.clearError()
            }
        }

        // Network selection buttons
        binding.btnMTN.setOnClickListener {
            viewModel.setNetwork("MTN")
        }

        binding.btnAirtel.setOnClickListener {
            viewModel.setNetwork("AIRTEL")
        }

        // Deposit button
        binding.btnDeposit.setOnClickListener {
            if (viewModel.isFormValid.value == true) {
                viewModel.initiateDeposit()
            }
        }
    }

    private fun updateNetworkSelection(network: String) {
        when (network) {
            "MTN" -> {
                // MTN selected
                binding.btnMTN.apply {
                    setBackgroundColor(getColor(R.color.primary_gold))
                    setTextColor(getColor(R.color.button_primary_text))
                }
                binding.btnAirtel.apply {
                    setBackgroundColor(getColor(R.color.input_background))
                    setTextColor(getColor(R.color.text_primary))
                }
            }
            "AIRTEL" -> {
                // Airtel selected
                binding.btnMTN.apply {
                    setBackgroundColor(getColor(R.color.input_background))
                    setTextColor(getColor(R.color.text_primary))
                }
                binding.btnAirtel.apply {
                    setBackgroundColor(getColor(R.color.primary_gold))
                    setTextColor(getColor(R.color.button_primary_text))
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}