package co.afrivest.ui.deposit

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import co.afrivest.data.model.Resource
import co.afrivest.data.repository.DepositRepository
import co.afrivest.databinding.ActivityBankTransferConfirmationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BankTransferConfirmationActivity : AppCompatActivity() {

    @Inject
    lateinit var depositRepository: DepositRepository

    private lateinit var binding: ActivityBankTransferConfirmationBinding
    private var countDownTimer: CountDownTimer? = null
    private var transactionId: Int = 0
    private var isConfirmed = false

    companion object {
        const val PREFS_NAME = "bank_transfer_prefs"
        const val KEY_PENDING_TRANSFER = "pending_bank_transfer"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBankTransferConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val type = intent.getStringExtra("TYPE") ?: "bank_transfer"
        val bankName = intent.getStringExtra("BANK_NAME")
        val sortCode = intent.getStringExtra("SORT_CODE")
        val accountNumber = intent.getStringExtra("ACCOUNT_NUMBER") ?: "N/A"
        val amount = intent.getStringExtra("AMOUNT") ?: "0"
        val currency = intent.getStringExtra("CURRENCY") ?: ""
        val reference = intent.getStringExtra("REFERENCE") ?: ""
        val expiration = intent.getStringExtra("EXPIRATION")
        transactionId = intent.getIntExtra("TRANSACTION_ID", 0)

        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_PENDING_TRANSFER, "$type|$bankName|$sortCode|$accountNumber|$amount|$currency|$reference|$expiration")
            .apply()

        supportActionBar?.title = if (type == "pay_with_bank_uk") "Pay With Bank" else "Bank Transfer"

        if (type == "pay_with_bank_uk") {
            binding.tvPrimaryLabel.text = "Sort Code"
            binding.tvPrimaryValue.text = sortCode ?: "N/A"
            binding.btnCopyPrimary.visibility = android.view.View.VISIBLE
        } else {
            binding.tvPrimaryLabel.text = "Bank Name"
            binding.tvPrimaryValue.text = bankName ?: "N/A"
            binding.btnCopyPrimary.visibility = android.view.View.GONE
        }

        binding.tvAccountNumber.text = accountNumber
        binding.tvAmount.text = "$currency $amount"
        binding.tvReference.text = reference

        binding.btnCopyPrimary.setOnClickListener { copyToClipboard("Sort Code", sortCode ?: "") }
        binding.btnCopyAccount.setOnClickListener { copyToClipboard("Account Number", accountNumber) }
        binding.btnCopyReference.setOnClickListener { copyToClipboard("Reference", reference) }

        binding.btnDone.setOnClickListener { finish() }

        setupExpiryTimer(expiration)
        startStatusPolling()
    }

    private fun startStatusPolling() {
        if (transactionId == 0) return
        lifecycleScope.launch {
            while (!isConfirmed && !isFinishing) {
                delay(8000)
                when (val result = depositRepository.checkDepositStatus(transactionId)) {
                    is Resource.Success -> {
                        val status = result.data?.status?.lowercase()
                        if (status == "success" || status == "successful") {
                            isConfirmed = true
                            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                                .remove(KEY_PENDING_TRANSFER)
                                .apply()
                            showConfirmed()
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun showConfirmed() {
        countDownTimer?.cancel()
        binding.rowExpiry.visibility = android.view.View.GONE
        android.app.AlertDialog.Builder(this)
            .setTitle("Payment Confirmed")
            .setMessage("Your wallet has been credited.")
            .setPositiveButton("OK") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun setupExpiryTimer(expiration: String?) {
        if (expiration.isNullOrEmpty()) return
        try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("UTC")
            val expiryDate = format.parse(expiration) ?: return
            val millisRemaining = expiryDate.time - System.currentTimeMillis()

            if (millisRemaining <= 0) return

            binding.rowExpiry.visibility = android.view.View.VISIBLE
            countDownTimer = object : CountDownTimer(millisRemaining, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val minutes = (millisUntilFinished / 1000) / 60
                    val seconds = (millisUntilFinished / 1000) % 60
                    binding.tvExpiry.text = String.format("%02d:%02d", minutes, seconds)
                }

                override fun onFinish() {
                    if (!isConfirmed) {
                        binding.tvExpiry.text = "Expired"
                        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                            .remove(KEY_PENDING_TRANSFER)
                            .apply()
                    }
                }
            }.start()
        } catch (e: Exception) {
            // Unparseable expiration — skip countdown, static details still shown
        }
    }

    private fun copyToClipboard(label: String, value: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}