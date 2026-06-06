package co.afrivest.ui.investments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import co.afrivest.R
import co.afrivest.data.api.InvestmentProduct
import co.afrivest.data.local.PreferencesManager
import co.afrivest.databinding.ActivityProductDetailBinding
import co.afrivest.ui.base.BaseActivity
import co.afrivest.utils.FeeCalculator
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProductDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private val viewModel: ProductDetailViewModel by viewModels()
    private var product: InvestmentProduct? = null

    @Inject lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        product = intent.getParcelableExtra(EXTRA_PRODUCT) ?: run { finish(); return }

        setupToolbar()
        setupProductDetails(product!!)
        setupPurchaseSection()
        setupObservers()

        // Fetch full product details
        product?.slug?.let { viewModel.loadFullProduct(it) }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = product?.title
        }
    }

    private fun setupProductDetails(p: InvestmentProduct) {
        val userCurrency = preferencesManager.defaultCurrency ?: "UGX"

        binding.apply {
            tvProductName.text = p.title
            tvPartnerName.text = p.partner?.name ?: ""
            tvCategoryName.text = p.category?.name ?: ""

            tvRiskLevel.text = (p.risk_level_label ?: p.risk_level ?: "").uppercase()
            val riskColor = when (p.risk_level?.lowercase()) {
                "very_low", "low" -> R.color.success_green
                "medium" -> R.color.warning_yellow
                "high", "very_high" -> R.color.error_red
                else -> R.color.text_secondary
            }
            tvRiskLevel.setTextColor(getColor(riskColor))

            tvReturns.text = if (p.expected_returns == "0.00" || p.expected_returns.isNullOrEmpty())
                "No Returns" else "${p.expected_returns}% p.a"

            tvDuration.text = p.duration_label
            tvAvailability.text = p.availability_status?.replaceFirstChar { it.uppercase() } ?: "Available"
            tvRating.text = "${p.rating_average} (${p.rating_count})"

            // Min investment in base currency
            tvMinLabel.text = "Min Investment (${p.currency})"
            tvMinimum.text = p.min_investment_formatted ?: p.min_investment

            // Min investment converted
            tvMinConvertedLabel.text = "Min Investment ($userCurrency)"
            val minRaw = p.min_investment?.toDoubleOrNull() ?: 0.0
            if (userCurrency != p.currency && minRaw > 0) {
                val rate = FeeCalculator.convertCurrency(minRaw, from = p.currency ?: "UGX", to = userCurrency, preferencesManager = preferencesManager)
                tvMinimumConverted.text = "$userCurrency ${FeeCalculator.formatCurrency(rate)}"
            } else {
                tvMinimumConverted.text = "${p.currency} ${FeeCalculator.formatCurrency(minRaw)}"
            }

            // Returns projection
            val returnsRate = p.expected_returns?.toDoubleOrNull() ?: 0.0
            val annualReturn = minRaw * returnsRate / 100
            tvAnnualReturnLabel.text = "Annual (${p.currency})"
            tvAnnualReturn.text = "${p.currency} ${formatSmartCurrency(annualReturn)}"
            tvAnnualReturnConvertedLabel.text = "Annual ($userCurrency)"
            val annualConverted = FeeCalculator.convertCurrency(annualReturn, from = p.currency ?: "UGX", to = userCurrency, preferencesManager = preferencesManager)
            tvAnnualReturnConverted.text = "$userCurrency ${formatSmartCurrency(annualConverted)}"

            // Short description
            if (!p.short_description.isNullOrBlank()) {
                tvDescriptionLabel.visibility = View.VISIBLE
                tvDescription.visibility = View.VISIBLE
                tvDescription.text = stripHtml(p.short_description)
            } else {
                tvDescriptionLabel.visibility = View.GONE
                tvDescription.visibility = View.GONE
            }

            // Full description
            val fullDesc = p.description
            if (!fullDesc.isNullOrBlank() && fullDesc != p.short_description) {
                tvFullDescriptionLabel.visibility = View.VISIBLE
                tvFullDescription.visibility = View.VISIBLE
                tvFullDescription.text = stripHtml(fullDesc)
            } else {
                tvFullDescriptionLabel.visibility = View.GONE
                tvFullDescription.visibility = View.GONE
            }

            // Features
            p.features?.let { features ->
                if (features.isNotEmpty()) {
                    tvFeaturesLabel.visibility = View.VISIBLE
                    tvFeatures.visibility = View.VISIBLE
                    tvFeatures.text = features.joinToString("\n") { "• $it" }
                }
            }

            // Terms
            if (!p.terms_conditions.isNullOrBlank()) {
                tvTermsLabel.visibility = View.VISIBLE
                tvTerms.visibility = View.VISIBLE
                tvTerms.text = p.terms_conditions
            }

            // Units
            if (p.total_units != null || p.units_available != null) {
                layoutUnits.visibility = View.VISIBLE
                tvTotalUnits.text = p.total_units?.toString() ?: "—"
                tvUnitsAvailable.text = p.units_available?.toString() ?: "—"
                val avail = p.units_available ?: 0
                tvUnitsAvailable.setTextColor(getColor(if (avail > 0) R.color.success_green else R.color.error_red))
            }

            // Documents
            p.documents?.let { docs ->
                if (docs.isNotEmpty()) {
                    tvDocumentsLabel.visibility = View.VISIBLE
                    layoutDocuments.visibility = View.VISIBLE
                    layoutDocuments.removeAllViews()
                    docs.forEach { doc ->
                        val row = layoutInflater.inflate(android.R.layout.simple_list_item_1, layoutDocuments, false) as TextView
                        row.text = "📄 ${doc.title} (${doc.type_label ?: doc.type ?: ""})"
                        row.setTextColor(getColor(R.color.text_primary))
                        row.textSize = 14f
                        layoutDocuments.addView(row)
                    }
                }
            }

            // Reviews
            p.reviews?.let { reviews ->
                if (reviews.isNotEmpty()) {
                    tvReviewsLabel.text = "Reviews (${reviews.size})"
                    tvReviewsLabel.visibility = View.VISIBLE
                    layoutReviews.visibility = View.VISIBLE
                    layoutReviews.removeAllViews()
                    reviews.forEach { review ->
                        val card = LinearLayout(this@ProductDetailActivity).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(32, 24, 32, 24)
                            setBackgroundResource(R.drawable.bg_card)
                            val params = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            params.bottomMargin = 16
                            layoutParams = params
                        }
                        val stars = "★".repeat(review.rating) + "☆".repeat(5 - review.rating)
                        val tv = TextView(this@ProductDetailActivity).apply {
                            text = "$stars\n${review.title ?: ""}\n${review.review ?: ""}\n— ${review.user_name ?: ""}"
                            setTextColor(getColor(R.color.text_secondary))
                            textSize = 13f
                        }
                        card.addView(tv)
                        layoutReviews.addView(card)
                    }
                }
            }

            // Image
            p.featured_image?.let { imageUrl ->
                Glide.with(this@ProductDetailActivity)
                    .load(imageUrl).placeholder(R.drawable.logo).error(R.drawable.logo)
                    .centerCrop().into(ivProductImage)
            }

            // Amount label
            tvAmountLabel.text = "Amount (${p.currency})"
            etAmount.setText(p.min_investment)
            tvMinimumAmount.text = "Minimum: ${p.min_investment_formatted ?: p.min_investment}"
        }
    }

    private fun setupPurchaseSection() {
        val userCurrency = preferencesManager.defaultCurrency ?: "UGX"

        binding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateAmount()
                val amt = s?.toString()?.toDoubleOrNull() ?: 0.0
                if (amt > 0 && userCurrency != (product?.currency ?: "UGX")) {
                    val converted = FeeCalculator.convertCurrency(amt, from = product?.currency ?: "UGX", to = userCurrency, preferencesManager = preferencesManager)
                    binding.tvAmountConverted.text = "≈ $userCurrency ${FeeCalculator.formatCurrency(converted)}"
                    binding.tvAmountConverted.visibility = View.VISIBLE
                } else {
                    binding.tvAmountConverted.visibility = View.GONE
                }
            }
        })

        binding.btnPurchase.setOnClickListener { purchaseProduct() }
    }

    private fun validateAmount(): Boolean {
        val amount = binding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
        val minAmount = product?.min_investment?.toDoubleOrNull() ?: 0.0
        val isValid = amount >= minAmount
        binding.btnPurchase.isEnabled = isValid
        return isValid
    }

    private fun purchaseProduct() {
        val amount = binding.etAmount.text.toString().toDoubleOrNull() ?: run {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }
        if (!validateAmount()) {
            Toast.makeText(this, "Amount must be at least ${product?.min_investment_formatted}", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.purchaseProduct(product?.id ?: return, amount, product?.currency ?: "UGX", binding.switchAutoReinvest.isChecked)
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingOverlay.root.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (!isLoading) validateAmount()
        }

        // When full product loads, refresh UI
        viewModel.product.observe(this) { fullProduct ->
            product = fullProduct
            setupProductDetails(fullProduct)
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

    private fun stripHtml(html: String): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
        } else {
            @Suppress("DEPRECATION")
            android.text.Html.fromHtml(html).toString().trim()
        }
    }

    private fun formatSmartCurrency(value: Double): String {
        if (value == 0.0) return "0"
        if (value >= 1.0) return FeeCalculator.formatCurrency(value)
        return String.format("%.4f", value)
    }

    companion object {
        const val EXTRA_PRODUCT = "product"
    }
}