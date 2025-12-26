package com.afrivest.app.ui.assets.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.afrivest.app.R
import com.afrivest.app.data.api.UserInvestment
import com.afrivest.app.databinding.ItemUserInvestmentBinding
import java.text.SimpleDateFormat
import java.util.Locale

class InvestmentsAdapter(
    private val onItemClick: (UserInvestment) -> Unit
) : ListAdapter<UserInvestment, InvestmentsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserInvestmentBinding.inflate(
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
        private val binding: ItemUserInvestmentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(investment: UserInvestment) {
            binding.apply {
                tvProductName.text = investment.product?.title ?: "Investment"
                tvPartnerName.text = investment.product?.partner?.name ?: ""

                tvStatus.text = investment.status.uppercase()
                val statusColor = when (investment.status.lowercase()) {
                    "active" -> R.color.success_green
                    "matured" -> R.color.primary_gold
                    "pending" -> R.color.warning_yellow
                    else -> R.color.text_secondary
                }
                tvStatus.setTextColor(root.context.getColor(statusColor))

                tvCurrentValue.text = investment.current_value
                tvReturns.text = "+${investment.returns_earned}"

                tvPurchaseDate.text = "Purchased: ${formatDate(investment.purchase_date)}"

                root.setOnClickListener {
                    onItemClick(investment)
                }
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                val date = parser.parse(dateString)
                date?.let { formatter.format(it) } ?: dateString
            } catch (e: Exception) {
                dateString
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<UserInvestment>() {
        override fun areItemsTheSame(oldItem: UserInvestment, newItem: UserInvestment) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: UserInvestment, newItem: UserInvestment) =
            oldItem == newItem
    }
}