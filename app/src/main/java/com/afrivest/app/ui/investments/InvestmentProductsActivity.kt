package com.afrivest.app.ui.investments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.afrivest.app.databinding.ActivityInvestmentProductsBinding
import com.afrivest.app.ui.investments.adapters.InvestmentProductsAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InvestmentProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInvestmentProductsBinding
    private val viewModel: InvestmentViewModel by viewModels()
    private lateinit var adapter: InvestmentProductsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvestmentProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupFilters()

        // Load products
        viewModel.loadProducts()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Investment Products"
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
            layoutManager = LinearLayoutManager(this@InvestmentProductsActivity)
            this.adapter = this@InvestmentProductsActivity.adapter
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

        viewModel.products.observe(this) { products ->
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
        // Category filter chip group
        binding.chipGroupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
            val categorySlug = when (checkedIds.firstOrNull()) {
                binding.chipTreasuryBonds.id -> "treasury-bonds"
                binding.chipUnitTrusts.id -> "unit-trusts"
                binding.chipFixedDeposits.id -> "fixed-deposits"
                binding.chipRealEstate.id -> "real-estate"
                else -> null
            }
            viewModel.filterByCategory(categorySlug)
        }

        // Sort button
        binding.btnSort.setOnClickListener {
            showSortDialog()
        }
    }

    private fun showSortDialog() {
        val sortOptions = arrayOf(
            "Returns (High to Low)",
            "Returns (Low to High)",
            "Min Investment (Low to High)",
            "Min Investment (High to Low)"
        )

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Sort By")
            .setItems(sortOptions) { _, which ->
                val sortBy = when (which) {
                    0 -> "returns_high"
                    1 -> "returns_low"
                    2 -> "min_investment_low"
                    3 -> "min_investment_high"
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