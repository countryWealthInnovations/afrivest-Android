package com.afrivest.app.ui.insurance.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.afrivest.app.R
import com.afrivest.app.data.api.InsuranceProvider
import com.afrivest.app.databinding.ItemInsuranceProviderBinding
import com.bumptech.glide.Glide

class InsuranceProvidersAdapter(
    private val onProviderClick: (InsuranceProvider) -> Unit
) : ListAdapter<InsuranceProvider, InsuranceProvidersAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInsuranceProviderBinding.inflate(
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
        private val binding: ItemInsuranceProviderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(provider: InsuranceProvider) {
            binding.apply {
                tvProviderName.text = provider.name

                provider.description?.let {
                    tvProviderDescription.text = it
                }

                // Load logo
                provider.logo_url?.let { logoUrl ->
                    Glide.with(root.context)
                        .load(logoUrl)
                        .placeholder(R.drawable.ic_insurance)
                        .error(R.drawable.ic_insurance)
                        .centerCrop()
                        .into(ivProviderLogo)
                }

                root.setOnClickListener {
                    onProviderClick(provider)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<InsuranceProvider>() {
        override fun areItemsTheSame(oldItem: InsuranceProvider, newItem: InsuranceProvider) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: InsuranceProvider, newItem: InsuranceProvider) =
            oldItem == newItem
    }
}