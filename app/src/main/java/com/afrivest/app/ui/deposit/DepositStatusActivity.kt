package com.afrivest.app.ui.deposit

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.afrivest.app.data.api.TransactionStatus
import com.afrivest.app.data.model.Resource
import com.afrivest.app.databinding.ActivityDepositStatusBinding
import com.afrivest.app.ui.main.MainActivity
import com.afrivest.app.utils.gone
import com.afrivest.app.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DepositStatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDepositStatusBinding
    private val viewModel: DepositViewModel by viewModels()

    private var transactionId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepositStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionId = intent.getIntExtra("TRANSACTION_ID", 0)
        val reference = intent.getStringExtra("REFERENCE") ?: ""
        val amount = intent.getStringExtra("AMOUNT") ?: ""
        val currency = intent.getStringExtra("CURRENCY") ?: ""
        val network = intent.getStringExtra("NETWORK") ?: ""

        setupUI(reference, amount, currency, network)
        setupObservers()
    }

    private fun setupUI(reference: String, amount: String, currency: String, network: String) {
        binding.textReference.text = reference
        binding.textAmount.text = "$amount $currency"
        binding.textNetwork.text = network
    }

    private fun setupObservers() {
        viewModel.statusResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading()
                }

                is Resource.Success -> {
                    resource.data?.let { status ->
                        when (status.status) {
                            "success" -> showSuccess(status)
                            "failed" -> showFailure(status)
                            "pending" -> showPending(status)
                        }
                    }
                }

                is Resource.Error -> {
                    showError(resource.message ?: "Unknown error")
                }
            }
        }
    }

    private fun showLoading() {
        binding.layoutLoading.visible()
        binding.layoutSuccess.gone()
        binding.layoutFailure.gone()
    }

    private fun showSuccess(status: TransactionStatus) {
        binding.layoutLoading.gone()
        binding.layoutSuccess.visible()
        binding.layoutFailure.gone()

        binding.btnDone.setOnClickListener {
            navigateToDashboard()
        }
    }

    private fun showFailure(status: TransactionStatus) {
        binding.layoutLoading.gone()
        binding.layoutSuccess.gone()
        binding.layoutFailure.visible()

        binding.textErrorMessage.text = status.error?.message ?: "Payment failed"

        binding.btnRetry.setOnClickListener {
            finish() // Go back to deposit screen
        }

        binding.btnCancel.setOnClickListener {
            navigateToDashboard()
        }
    }

    private fun showPending(status: TransactionStatus) {
        binding.layoutLoading.visible()
        binding.textStatus.text = status.message ?: "Processing payment..."
    }

    private fun showError(message: String) {
        binding.layoutLoading.gone()
        binding.layoutFailure.visible()
        binding.textErrorMessage.text = message

        binding.btnCancel.setOnClickListener {
            navigateToDashboard()
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {
        // Prevent back press while processing
        if (binding.layoutLoading.visibility == android.view.View.VISIBLE) {
            // Do nothing - force user to wait
        } else {
            super.onBackPressed()
        }
    }
}