package com.afrivest.app.ui.investments.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.afrivest.app.R
import com.afrivest.app.data.api.InvestmentProduct
import com.afrivest.app.databinding.ItemInvestmentProductBinding
import com.bumptech.glide.Glide

class InvestmentProductsAdapter(
    private val onProductClick: (InvestmentProduct) -> Unit
) : ListAdapter<InvestmentProduct, InvestmentProductsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInvestmentProductBinding.inflate(
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
        private val binding: ItemInvestmentProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: InvestmentProduct) {
            binding.apply {
                // Product name
                tvProductName.text = product.name

                // Partner name
                tvPartnerName.text = product.partner?.name ?: product.category?.name

                // Returns
                val returns = product.expected_returns
                tvReturns.text = if (returns == "0.00" || returns!!.isEmpty()) {
                    "No Returns"
                } else {
                    "$returns% p.a"
                }

                // Lock-in period
                tvLockIn.text = product.duration_label

                // Minimum investment
                tvMinInvestment.text = product.min_investment_formatted

                // Risk level
                tvRiskLevel.text = product.risk_level_label
                val riskColor = when (product.risk_level!!.lowercase()) {
                    "very_low", "low" -> R.color.success_green
                    "medium" -> R.color.warning_yellow
                    "high", "very_high" -> R.color.error_red
                    else -> R.color.text_secondary
                }
                tvRiskLevel.setTextColor(root.context.getColor(riskColor))

                // Load image
                product.image_url?.let { imageUrl ->
                    Glide.with(root.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.logo)
                        .error(R.drawable.logo)
                        .centerCrop()
                        .into(ivProductImage)
                }

                root.setOnClickListener {
                    onProductClick(product)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<InvestmentProduct>() {
        override fun areItemsTheSame(oldItem: InvestmentProduct, newItem: InvestmentProduct) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: InvestmentProduct, newItem: InvestmentProduct) =
            oldItem == newItem
    }
}