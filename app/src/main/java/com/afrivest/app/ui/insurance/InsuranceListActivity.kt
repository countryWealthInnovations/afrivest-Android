package com.afrivest.app.ui.insurance

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.afrivest.app.databinding.ActivityInsuranceListBinding
import com.afrivest.app.ui.investments.ProductDetailActivity
import com.afrivest.app.ui.investments.adapters.InvestmentProductsAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InsuranceListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInsuranceListBinding
    private val viewModel: InsuranceViewModel by viewModels()
    private lateinit var adapter: InvestmentProductsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInsuranceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupFilters()

        // Load insurance products
        viewModel.loadInsuranceProducts()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Insurance Products"
        }
    }

    private fun setupRecyclerView() {
        adapter = InvestmentProductsAdapter { product ->
            val intent = Intent(this, ProductDetailActivity::class.java).apply {
                putExtra(ProductDetailActivity.EXTRA_PRODUCT, product)
            }
            startActivity(intent)
        }

        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(this@InsuranceListActivity)
            this.adapter = this@InsuranceListActivity.adapter
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.loadingOverlay.root.visibility = View.VISIBLE
            } else {
                binding.loadingOverlay.root.visibility = View.GONE
            }
        }

        viewModel.insuranceProducts.observe(this) { products ->
            if (products.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvProducts.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.rvProducts.visibility = View.VISIBLE
                adapter.submitList(products)
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupFilters() {
        // Load providers for filter
        viewModel.loadProviders()

        viewModel.providers.observe(this) { providers ->
            if (providers.isNotEmpty()) {
                setupProviderFilter(providers)
            }
        }

        // Sort button
        binding.btnSort.setOnClickListener {
            showSortDialog()
        }
    }

    private fun setupProviderFilter(providers: List<com.afrivest.app.data.api.InsuranceProvider>) {
        // Add "All" chip
        val allChip = com.google.android.material.chip.Chip(this).apply {
            text = "All"
            isCheckable = true
            isChecked = true
            setOnClickListener {
                viewModel.filterByProvider(null)
            }
        }
        binding.chipGroupProvider.addView(allChip)

        // Add provider chips
        providers.forEach { provider ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = provider.name
                isCheckable = true
                setOnClickListener {
                    viewModel.filterByProvider(provider.id)
                }
            }
            binding.chipGroupProvider.addView(chip)
        }
    }

    private fun showSortDialog() {
        val sortOptions = arrayOf(
            "Coverage (High to Low)",
            "Coverage (Low to High)",
            "Premium (Low to High)",
            "Premium (High to Low)"
        )

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Sort By")
            .setItems(sortOptions) { _, which ->
                val sortBy = when (which) {
                    0 -> "coverage_high"
                    1 -> "coverage_low"
                    2 -> "premium_low"
                    3 -> "premium_high"
                    else -> null
                }
                sortBy?.let { viewModel.sortProducts(it) }
            }
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}