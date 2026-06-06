package co.afrivest.ui.assets.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.afrivest.R
import co.afrivest.data.api.InsuranceClaim
import co.afrivest.databinding.ItemClaimBinding
import java.text.SimpleDateFormat
import java.util.*

class ClaimsAdapter : ListAdapter<InsuranceClaim, ClaimsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClaimBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemClaimBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(claim: InsuranceClaim) {
            binding.apply {
                tvClaimNumber.text = claim.claim_number
                tvClaimType.text = claim.claim_type.capitalize(Locale.ROOT)

                tvStatus.text = claim.status.uppercase()
                val statusColor = when (claim.status.lowercase()) {
                    "approved" -> R.color.success_green
                    "rejected" -> R.color.error_red
                    "pending" -> R.color.warning_yellow
                    "processing" -> R.color.primary_gold
                    else -> R.color.text_secondary
                }
                tvStatus.setTextColor(root.context.getColor(statusColor))
                tvStatus.setBackgroundColor(adjustColorAlpha(root.context.getColor(statusColor), 0.1f))

                tvAmount.text = claim.amount_formatted
                tvFiledDate.text = "Filed: ${formatDate(claim.created_at)}"

                claim.description?.let {
                    tvDescription.text = it
                }
            }
        }

        private fun formatDate(dateString: String): String {
            val formats = listOf(
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
                "yyyy-MM-dd"
            )
            val outputFormatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
            for (format in formats) {
                try {
                    val parser = SimpleDateFormat(format, Locale.US)
                    val date = parser.parse(dateString)
                    if (date != null) return outputFormatter.format(date)
                } catch (e: Exception) { continue }
            }
            return dateString
        }

        private fun adjustColorAlpha(color: Int, factor: Float): Int {
            val alpha = (255 * factor).toInt()
            val red = android.graphics.Color.red(color)
            val green = android.graphics.Color.green(color)
            val blue = android.graphics.Color.blue(color)
            return android.graphics.Color.argb(alpha, red, green, blue)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<InsuranceClaim>() {
        override fun areItemsTheSame(oldItem: InsuranceClaim, newItem: InsuranceClaim) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: InsuranceClaim, newItem: InsuranceClaim) =
            oldItem == newItem
    }
}