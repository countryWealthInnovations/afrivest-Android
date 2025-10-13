package com.afrivest.app.ui.transfer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.afrivest.app.R
import com.afrivest.app.data.model.Resource
import com.afrivest.app.databinding.ActivityWithdrawBinding
import com.afrivest.app.utils.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WithdrawActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWithdrawBinding
    private val viewModel: WithdrawViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWithdrawBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupObservers()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Withdraw Money"
    }

    private fun setupObservers() {
        // Form Valid
        viewModel.isFormValid.observe(this) { isValid ->
            if (isValid) {
                binding.btnWithdraw.enable()
            } else {
                binding.btnWithdraw.disable()
            }
        }

        // Observe fee changes
        viewModel.totalAmount.observe(this) { total ->
            if (total > 0) {
                binding.feeSection.visibility = View.VISIBLE

                // Show current amount
                val currentAmount = viewModel.amount.value?.toDoubleOrNull() ?: 0.0
                binding.tvWithdrawAmount.text = "UGX ${FeeCalculator.formatCurrency(currentAmount)}"

                binding.tvFeeAmount.text = "UGX ${FeeCalculator.formatCurrency(viewModel.fee.value ?: 0.0)}"
                binding.tvTotalAmount.text = "UGX ${FeeCalculator.formatCurrency(total)}"

                val balanceAfter = viewModel.balanceAfterWithdrawal.value ?: 0.0
                binding.tvBalanceAfter.text = "UGX ${FeeCalculator.formatCurrency(balanceAfter)}"

                // Change color based on insufficient funds
                if (viewModel.insufficientFundsWarning.value == true) {
                    binding.tvBalanceAfter.setTextColor(getColor(R.color.error_red))
                } else {
                    binding.tvBalanceAfter.setTextColor(getColor(R.color.success_green))
                }
            } else {
                binding.feeSection.visibility = View.GONE
            }
        }

        // Withdraw Result
        viewModel.withdrawResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visible()
                    binding.btnWithdraw.disable()
                    binding.textError.gone()
                }

                is Resource.Success -> {
                    binding.progressBar.gone()
                    resource.data?.let { withdrawResponse ->
                        // Navigate to success screen
                        val intent = Intent(this, WithdrawSuccessActivity::class.java).apply {
                            putExtra("WITHDRAW_RESPONSE", withdrawResponse)
                            putExtra("PHONE_NUMBER", "+256${viewModel.phoneNumber.value}")
                        }
                        startActivity(intent)
                        finish()
                    }
                }

                is Resource.Error -> {
                    binding.progressBar.gone()
                    binding.btnWithdraw.enable()
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

            // Show detected network
            when {
                phone.startsWith("77") || phone.startsWith("78") ||
                        phone.startsWith("76") || phone.startsWith("79") -> {
                    binding.tvPhoneHelper.text = "MTN detected ✓"
                    binding.tvPhoneHelper.setTextColor(getColor(R.color.success_green))
                }
                phone.startsWith("70") || phone.startsWith("74") ||
                        phone.startsWith("75") -> {
                    binding.tvPhoneHelper.text = "Airtel detected ✓"
                    binding.tvPhoneHelper.setTextColor(getColor(R.color.success_green))
                }
                else -> {
                    binding.tvPhoneHelper.text = "MTN: 77, 78, 76, 79 | Airtel: 70, 74, 75"
                    binding.tvPhoneHelper.setTextColor(getColor(R.color.text_secondary))
                }
            }

            // Validate phone length (9 digits for Uganda)
            if (phone.length == 9) {
                binding.tvPhoneHelper.text = "${binding.tvPhoneHelper.text} - Valid ✓"
                binding.tvPhoneHelper.setTextColor(getColor(R.color.success_green))
            } else if (phone.length > 9) {
                binding.tvPhoneHelper.text = "Phone number too long"
                binding.tvPhoneHelper.setTextColor(getColor(R.color.error_red))
            }
        }

        // Withdraw button
        binding.btnWithdraw.setOnClickListener {
            if (viewModel.isFormValid.value == true) {
                viewModel.initiateWithdraw()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}