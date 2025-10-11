package com.afrivest.app.ui.transactions

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.afrivest.app.R
import com.afrivest.app.data.model.Transaction
import com.afrivest.app.databinding.ActivityTransactionDetailBinding
import java.text.SimpleDateFormat
import java.util.*

class TransactionDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionDetailBinding
    private lateinit var transaction: Transaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get transaction from intent
        transaction = intent.getParcelableExtra("transaction") ?: run {
            finish()
            return
        }

        setupToolbar()
        displayTransactionDetails()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun displayTransactionDetails() {
        // Transaction Reference
        binding.tvReference.text = transaction.reference

        // Transaction Type
        binding.tvType.text = transaction.getTypeDisplayName()

        // Amount
        val sign = when (transaction.type) {
            "deposit" -> "+"
            else -> "-"
        }
        binding.tvAmount.text = String.format(
            "%s %,.2f %s",
            sign,
            transaction.getAmountDouble(),
            transaction.currency
        )

        // Set amount color
        binding.tvAmount.setTextColor(
            when (transaction.type) {
                "deposit" -> getColor(R.color.success_green)
                "withdrawal", "transfer" -> getColor(R.color.error_red)
                else -> getColor(R.color.text_primary)
            }
        )

        // Status
        binding.tvStatus.text = transaction.status.capitalize(Locale.ROOT)
        binding.tvStatus.setTextColor(transaction.getStatusColor())
        binding.tvStatus.setBackgroundColor(
            adjustColorAlpha(transaction.getStatusColor(), 0.2f)
        )

        // Date
        binding.tvDate.text = formatDate(transaction.created_at)

        // Description
        if (!transaction.description.isNullOrEmpty()) {
            binding.tvDescription.text = transaction.description
        } else {
            binding.tvDescription.text = "No description"
        }

        // Fee
        if (!transaction.fee_amount.isNullOrEmpty() && transaction.getFeeDouble() > 0) {
            binding.tvFee.text = String.format(
                "%,.2f %s",
                transaction.getFeeDouble(),
                transaction.currency
            )
        } else {
            binding.tvFee.text = "0.00 ${transaction.currency}"
        }

        // Total
        if (!transaction.total_amount.isNullOrEmpty()) {
            binding.tvTotal.text = String.format(
                "%,.2f %s",
                transaction.getTotalDouble(),
                transaction.currency
            )
        } else {
            binding.tvTotal.text = String.format(
                "%,.2f %s",
                transaction.getAmountDouble() + transaction.getFeeDouble(),
                transaction.currency
            )
        }

        // Payment Channel
        if (!transaction.payment_channel.isNullOrEmpty()) {
            binding.tvPaymentChannel.text = transaction.payment_channel!!.capitalize(Locale.ROOT)
        } else {
            binding.tvPaymentChannel.text = "N/A"
        }

        // Recipient (if transfer)
        if (transaction.type == "transfer" && transaction.recipient != null) {
            binding.llRecipient.visibility = android.view.View.VISIBLE
            binding.tvRecipientName.text = transaction.recipient!!.name
            binding.tvRecipientEmail.text = transaction.recipient!!.email
        } else {
            binding.llRecipient.visibility = android.view.View.GONE
        }

        // External Reference
        if (!transaction.external_reference.isNullOrEmpty()) {
            binding.tvExternalReference.text = transaction.external_reference
        } else {
            binding.tvExternalReference.text = "N/A"
        }

        // Completed At
        if (!transaction.completed_at.isNullOrEmpty()) {
            binding.tvCompletedAt.text = formatDate(transaction.completed_at!!)
        } else {
            binding.tvCompletedAt.text = "Not completed"
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString)

            val outputFormat = SimpleDateFormat("MMM d, yyyy 'at' hh:mm a", Locale.US)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    private fun adjustColorAlpha(color: Int, factor: Float): Int {
        val alpha = (255 * factor).toInt()
        val red = android.graphics.Color.red(color)
        val green = android.graphics.Color.green(color)
        val blue = android.graphics.Color.blue(color)
        return android.graphics.Color.argb(alpha, red, green, blue)
    }
}