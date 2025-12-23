package com.afrivest.app.ui.marketplace

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.afrivest.app.databinding.ActivityGoldMarketplaceBinding
import com.afrivest.app.ui.investments.adapters.InvestmentProductsAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoldMarketplaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoldMarketplaceBinding
    private val viewModel: MarketplaceViewModel by viewModels()
    private lateinit var adapter: InvestmentProductsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoldMarketplaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupObservers()

        // Load gold products and price
        viewModel.loadGoldProducts()
        viewModel.loadCurrentGoldPrice()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Gold Marketplace"
        }
    }

    private fun setupRecyclerView() {
        adapter = InvestmentProductsAdapter { product ->
            // TODO: Navigate to product details
            Toast.makeText(this, "Gold Product: ${product.name}", Toast.LENGTH_SHORT).show()
        }

        binding.rvGoldProducts.apply {
            layoutManager = LinearLayoutManager(this@GoldMarketplaceActivity)
            this.adapter = this@GoldMarketplaceActivity.adapter
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

        viewModel.goldProducts.observe(this) { products ->
            if (products.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvGoldProducts.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.rvGoldProducts.visibility = View.VISIBLE
                adapter.submitList(products)
            }
        }

        viewModel.goldPrice.observe(this) { goldPrice ->
            goldPrice?.let {
                binding.tvGoldPrice.text = "Current Price: UGX ${String.format("%,.0f", it.price_per_gram_ugx)}/g"
                binding.cardGoldPrice.visibility = View.VISIBLE
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}