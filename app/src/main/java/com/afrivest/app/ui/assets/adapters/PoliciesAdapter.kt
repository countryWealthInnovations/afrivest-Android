package com.afrivest.app.ui.assets.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.afrivest.app.R
import com.afrivest.app.data.api.InsurancePolicy
import com.afrivest.app.databinding.ItemPolicyBinding

class PoliciesAdapter(
    private val onItemClick: (InsurancePolicy) -> Unit
) : ListAdapter<InsurancePolicy, PoliciesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPolicyBinding.inflate(
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
        private val binding: ItemPolicyBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(policy: InsurancePolicy) {
            binding.apply {
                tvPolicyNumber.text = policy.policy_number
                tvPolicyType.text = policy.policy_type.replace("_", " ").uppercase()

                tvStatus.text = policy.status.uppercase()
                val statusColor = when (policy.status.lowercase()) {
                    "active" -> R.color.success_green
                    "expired" -> R.color.text_secondary
                    "cancelled" -> R.color.error_red
                    "pending" -> R.color.warning_yellow
                    else -> R.color.text_secondary
                }
                tvStatus.setTextColor(root.context.getColor(statusColor))

                tvCoverage.text = policy.coverage_amount
                tvPremium.text = policy.premium_amount

                policy.partner?.let {
                    tvProvider.text = "Provider: ${it.name}"
                }

                root.setOnClickListener {
                    onItemClick(policy)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<InsurancePolicy>() {
        override fun areItemsTheSame(oldItem: InsurancePolicy, newItem: InsurancePolicy) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: InsurancePolicy, newItem: InsurancePolicy) =
            oldItem == newItem
    }
}