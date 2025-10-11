package com.afrivest.app.ui.history.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.afrivest.app.R
import com.afrivest.app.data.model.Transaction
import com.afrivest.app.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.*

class TransactionHistoryAdapter(
    private val onItemClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionHistoryAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            // Set transaction description
            binding.tvTransactionDescription.text = getTransactionDescription(transaction)

            // Set transaction date
            binding.tvTransactionDate.text = formatDate(transaction.created_at)

            // Set amount with sign
            val sign = getAmountSign(transaction)
            val formattedAmount = String.format(
                "%s %,.2f %s",
                sign,
                transaction.getAmountDouble(),
                transaction.currency
            )
            binding.tvAmount.text = formattedAmount

            // Set amount color based on transaction type and direction
            binding.tvAmount.setTextColor(getAmountColor(transaction))

            // Set status
            binding.tvStatus.text = transaction.status.capitalize(Locale.ROOT)
            binding.tvStatus.setTextColor(transaction.getStatusColor())
            binding.tvStatus.setBackgroundColor(
                adjustColorAlpha(transaction.getStatusColor(), 0.2f)
            )

            // Set transaction icon
            setTransactionIcon(transaction)

            // Set click listener
            binding.root.setOnClickListener {
                onItemClick(transaction)
            }
        }

        private fun getTransactionDescription(transaction: Transaction): String {
            return when {
                !transaction.description.isNullOrEmpty() -> transaction.description
                transaction.type == "transfer" -> {
                    when (transaction.direction) {
                        "sent" -> "Transfer to ${transaction.other_party?.name ?: "Unknown"}"
                        "received" -> "Transfer from ${transaction.other_party?.name ?: "Unknown"}"
                        else -> transaction.getTypeDisplayName()
                    }
                }
                else -> transaction.getTypeDisplayName()
            }
        }

        private fun getAmountSign(transaction: Transaction): String {
            return when {
                transaction.direction == "received" -> "+"
                transaction.type == "deposit" -> "+"
                transaction.type in listOf("withdrawal", "transfer", "bill_payment", "insurance",
                    "investment", "gold_purchase", "crypto_purchase") -> "-"
                else -> ""
            }
        }

        private fun getAmountColor(transaction: Transaction): Int {
            return when {
                transaction.direction == "received" -> itemView.context.getColor(R.color.success_green)
                transaction.type == "deposit" -> itemView.context.getColor(R.color.success_green)
                transaction.type in listOf("withdrawal", "transfer") -> itemView.context.getColor(R.color.error_red)
                else -> itemView.context.getColor(R.color.text_primary)
            }
        }

        private fun setTransactionIcon(transaction: Transaction) {
            val iconRes = when (transaction.type) {
                "deposit" -> R.drawable.ic_arrow_down
                "withdrawal" -> R.drawable.ic_chevron_up
                "transfer" -> R.drawable.ic_arrow_right
                "bill_payment" -> R.drawable.ic_receipt
                "insurance" -> R.drawable.ic_insurance
                "investment" -> R.drawable.ic_chart
                "gold_purchase" -> R.drawable.ic_circle
                "crypto_purchase" -> R.drawable.ic_crypto
                else -> R.drawable.ic_transaction
            }

            binding.ivTransactionIcon.setImageResource(iconRes)

            // Set icon tint based on type
            val iconColor = when (transaction.type) {
                "deposit" -> itemView.context.getColor(R.color.success_green)
                "withdrawal" -> itemView.context.getColor(R.color.error_red)
                else -> itemView.context.getColor(R.color.primary_gold)
            }

            binding.ivTransactionIcon.setColorFilter(iconColor)
            binding.vIconBackground.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    adjustColorAlpha(iconColor, 0.2f)
                )
        }

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US)
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(dateString)

                val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
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

    class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}