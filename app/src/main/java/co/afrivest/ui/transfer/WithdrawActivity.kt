package co.afrivest.ui.transfer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import co.afrivest.R
import co.afrivest.data.model.Resource
import co.afrivest.databinding.ActivityWithdrawBinding
import co.afrivest.ui.base.BaseActivity
import co.afrivest.utils.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WithdrawActivity : BaseActivity() {

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
        viewModel.isFormValid.observe(this) { isValid ->
            if (isValid) binding.btnWithdraw.enable() else binding.btnWithdraw.disable()
        }

        viewModel.payoutMethod.observe(this) { method ->
            if (method == PayoutMethod.MOBILE_MONEY) {
                binding.layoutMobileMoney.visible()
                binding.layoutBankTransfer.gone()
                binding.layoutNetworkPicker.visible()
            } else {
                binding.layoutMobileMoney.gone()
                binding.layoutBankTransfer.visible()
                binding.layoutNetworkPicker.gone()
            }
            updateCurrencyChips()
        }

        viewModel.selectedCurrency.observe(this) { currency ->
            binding.tvAmountLabel.text = "Amount ($currency)"
            updateNetworkChips()
            val dialCode = when (currency) {
                "UGX" -> "+256"
                "KES" -> "+254"
                "NGN" -> "+234"
                "GHS" -> "+233"
                "TZS" -> "+255"
                "RWF" -> "+250"
                "ZMW" -> "+260"
                "ZAR" -> "+27"
                "XAF", "XOF" -> "+237"
                else -> ""
            }
            binding.tvCountryCode.text = dialCode
            binding.tvCountryCode.visibility = if (dialCode.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.totalAmount.observe(this) { total ->
            if (total > 0) {
                binding.feeSection.visible()
                val currency = viewModel.selectedCurrency.value ?: "UGX"
                val amount = viewModel.amount.value?.toDoubleOrNull() ?: 0.0
                binding.tvWithdrawAmount.text = "$currency ${FeeCalculator.formatCurrency(amount)}"
                binding.tvTransactionFeeAmount.text = "$currency ${FeeCalculator.formatCurrency(viewModel.transactionFee.value ?: 0.0)}"
                binding.tvTotalAmount.text = "$currency ${FeeCalculator.formatCurrency(total)}"
                val walletCur = viewModel.walletCurrency.value ?: "UGX"
                val balanceAfter = viewModel.balanceAfterWithdrawal.value ?: 0.0
                binding.tvBalanceAfter.text = "$walletCur ${FeeCalculator.formatCurrency(balanceAfter)}"
                binding.tvBalanceAfter.setTextColor(
                    if (viewModel.insufficientFundsWarning.value == true) getColor(R.color.error_red)
                    else getColor(R.color.success_green)
                )
                binding.tvFeeNote.text = if (viewModel.feeConfirmedByAPI.value == true)
                    "✓ Fees confirmed" else "* Estimated — confirmed on submit"
            } else {
                binding.feeSection.gone()
            }
        }

        viewModel.withdrawResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visible()
                    binding.btnWithdraw.disable()
                    binding.textError.gone()
                }
                is Resource.Success -> {
                    binding.progressBar.gone()
                    resource.data?.let { response ->
                        val intent = Intent(this, WithdrawSuccessActivity::class.java).apply {
                            putExtra("WITHDRAW_RESPONSE", response)
                            putExtra("PHONE_NUMBER", viewModel.phoneNumber.value ?: "")
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

    private fun updateCurrencyChips() {
        binding.chipGroupCurrency.removeAllViews()
        val currencies = if (viewModel.payoutMethod.value == PayoutMethod.MOBILE_MONEY)
            viewModel.mobileMoneyNetworks.keys.sorted()
        else viewModel.bankCurrencies
        currencies.forEach { currency ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = currency
                isCheckable = true
                isChecked = currency == viewModel.selectedCurrency.value
                setOnClickListener { viewModel.setSelectedCurrency(currency) }
            }
            binding.chipGroupCurrency.addView(chip)
        }
    }

    private fun updateNetworkChips() {
        binding.chipGroupNetwork.removeAllViews()
        viewModel.getAvailableNetworks().forEach { network ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = network
                isCheckable = true
                isChecked = network == viewModel.selectedNetwork.value
                setOnClickListener { viewModel.setNetwork(network) }
            }
            binding.chipGroupNetwork.addView(chip)
        }
    }

    private fun setupListeners() {
        binding.togglePayoutMethod.addOnButtonCheckedListener { _: com.google.android.material.button.MaterialButtonToggleGroup, checkedId: Int, isChecked: Boolean ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnMobileMoney -> viewModel.setPayoutMethod(PayoutMethod.MOBILE_MONEY)
                    R.id.btnBankTransfer -> viewModel.setPayoutMethod(PayoutMethod.BANK_TRANSFER)
                }
            }
        }

        binding.editTextAmount.doOnTextChanged { text, _, _, _ ->
            viewModel.setAmount(text?.toString() ?: "")
        }

        binding.editTextPhone.doOnTextChanged { text, _, _, _ ->
            val phone = text?.toString() ?: ""
            viewModel.setPhoneNumber(phone)
            binding.tvPhoneHelper.text = when {
                phone.length >= 7 -> "Valid ✓"
                phone.isNotEmpty() -> "Enter full phone number"
                else -> "Enter phone number"
            }
            binding.tvPhoneHelper.setTextColor(
                if (phone.length >= 7) getColor(R.color.success_green)
                else getColor(R.color.text_secondary)
            )
        }

        binding.editTextBankCode.doOnTextChanged { text, _, _, _ ->
            viewModel.setBankCode(text?.toString() ?: "")
        }
        binding.editTextAccountNumber.doOnTextChanged { text, _, _, _ ->
            viewModel.setAccountNumber(text?.toString() ?: "")
        }
        binding.editTextAccountName.doOnTextChanged { text, _, _, _ ->
            viewModel.setAccountName(text?.toString() ?: "")
        }

        binding.btnWithdraw.setOnClickListener {
            if (viewModel.isFormValid.value == true) viewModel.initiateWithdraw()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}