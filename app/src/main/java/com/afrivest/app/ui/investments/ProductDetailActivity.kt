package com.afrivest.app.ui.investments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.afrivest.app.R
import com.afrivest.app.data.api.InvestmentProduct
import com.afrivest.app.databinding.ActivityProductDetailBinding
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private val viewModel: ProductDetailViewModel by viewModels()
    private lateinit var product: InvestmentProduct

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get product from intent
        @Suppress("DEPRECATION")
        product = intent.getParcelableExtra("product") ?: run {
            finish()
            return
        }

        setupToolbar()
        setupProductDetails()
        setupPurchaseSection()
        setupObservers()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = product.title
        }
    }

    private fun setupProductDetails() {
        binding.apply {
            // Product name
            tvProductName.text = product.title

            // Partner name
            tvPartnerName.text = product.partner?.name ?: product.category?.name

            // Risk level
            tvRiskLevel.text = product.risk_level_label
            val riskColor = when (product.risk_level.lowercase()) {
                "very_low", "low" -> R.color.success_green
                "medium" -> R.color.warning_yellow
                "high", "very_high" -> R.color.error_red
                else -> R.color.text_secondary
            }
            tvRiskLevel.setTextColor(getColor(riskColor))

            // Stats
            tvReturnsLabel.text = "Expected Returns"
            tvReturns.text = if (product.expected_returns == "0.00" || product.expected_returns.isEmpty()) {
                "No Returns"
            } else {
                "${product.expected_returns}% p.a"
            }

            tvDuration.text = product.duration_label
            tvMinimum.text = product.min_investment_formatted
            tvCurrency.text = product.currency

            // Description
            product.short_description?.let {
                tvDescription.text = it
            } ?: run {
                tvDescriptionLabel.visibility = View.GONE
                tvDescription.visibility = View.GONE
            }

            // Features
            product.features?.let { features ->
                tvFeaturesLabel.visibility = View.VISIBLE
                tvFeatures.visibility = View.VISIBLE
                tvFeatures.text = features.joinToString("\n") { "• $it" }
            } ?: run {
                tvFeaturesLabel.visibility = View.GONE
                tvFeatures.visibility = View.GONE
            }

            // Load image
            product.featured_image?.let { imageUrl ->
                Glide.with(this@ProductDetailActivity)
                    .load(imageUrl)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .centerCrop()
                    .into(ivProductImage)
            }
        }
    }

    private fun setupPurchaseSection() {
        binding.apply {
            // Set minimum amount
            etAmount.setText(product.min_investment)
            tvMinimumAmount.text = "Minimum: ${product.min_investment_formatted}"

            // Amount validation
            etAmount.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    validateAmount()
                }
            })

            // Purchase button
            btnPurchase.setOnClickListener {
                purchaseProduct()
            }
        }
    }

    private fun validateAmount(): Boolean {
        val amountText = binding.etAmount.text.toString()
        val amount = amountText.toDoubleOrNull() ?: 0.0
        val minAmount = product.min_investment.toDoubleOrNull() ?: 0.0

        val isValid = amount >= minAmount
        binding.btnPurchase.isEnabled = isValid

        return isValid
    }

    private fun purchaseProduct() {
        val amountText = binding.etAmount.text.toString()
        val amount = amountText.toDoubleOrNull() ?: run {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (!validateAmount()) {
            Toast.makeText(
                this,
                "Amount must be at least ${product.min_investment_formatted}",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        viewModel.purchaseProduct(product.id, amount, product.currency)
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.loadingOverlay.root.visibility = View.VISIBLE
                binding.btnPurchase.isEnabled = false
            } else {
                binding.loadingOverlay.root.visibility = View.GONE
                validateAmount()
            }
        }

        viewModel.purchaseSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Purchase successful!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        const val EXTRA_PRODUCT = "product"
    }
}