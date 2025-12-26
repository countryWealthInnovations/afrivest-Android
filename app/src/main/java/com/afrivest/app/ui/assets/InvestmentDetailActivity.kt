package com.afrivest.app.ui.assets

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.afrivest.app.R
import com.afrivest.app.data.api.UserInvestment
import com.afrivest.app.databinding.ActivityInvestmentDetailBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class InvestmentDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInvestmentDetailBinding
    private lateinit var investment: UserInvestment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvestmentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        investment = intent.getParcelableExtra("investment") ?: run {
            finish()
            return
        }

        setupToolbar()
        setupViews()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Investment Details"
        }
    }

    private fun setupViews() {
        // Header
        binding.tvProductName.text = investment.product?.title ?: "Investment"
        binding.tvPartnerName.text = investment.product?.partner?.name ?: ""
        binding.tvInvestmentCode.text = investment.investment_code

        // Status
        binding.tvStatus.text = investment.status.uppercase()
        val statusColor = when (investment.status.lowercase()) {
            "active" -> R.color.success_green
            "matured" -> R.color.primary_gold
            "pending" -> R.color.warning_yellow
            else -> R.color.text_secondary
        }
        binding.tvStatus.setTextColor(getColor(statusColor))
        binding.tvStatus.setBackgroundColor(getColor(statusColor).adjustAlpha(0.1f))

        // Performance
        binding.tvInvested.text = investment.amount_invested_formatted
        binding.tvCurrentValue.text = investment.current_value_formatted

        val invested = investment.amount_invested.toDoubleOrNull() ?: 0.0
        val current = investment.current_value.toDoubleOrNull() ?: 0.0
        val returns = current - invested

        binding.tvReturns.text = "+${formatAmount(returns)} ${investment.currency}"
        binding.tvReturnsPercent.text = String.format("%.2f%%", investment.returns_percentage)

        // Maturity Tracking
        binding.tvPurchaseDate.text = formatDate(investment.purchase_date)

        investment.maturity_date?.let { maturityDate ->
            binding.tvMaturityDate.text = formatDate(maturityDate)

            val progress = calculateProgress(investment.purchase_date, maturityDate)
            binding.progressBar.progress = (progress * 100).toInt()
            binding.tvProgress.text = String.format("%.1f%%", progress * 100)

            val days = calculateDaysRemaining(maturityDate)
            binding.tvDaysRemaining.text = if (days > 0) "$days days" else "Matured"
        } ?: run {
            binding.tvMaturityDate.text = "N/A"
            binding.progressBar.progress = 0
            binding.tvProgress.text = "0%"
            binding.tvDaysRemaining.text = "N/A"
        }

        // Projected Returns
        val monthsToMaturity = calculateMonthsToMaturity(
            investment.purchase_date,
            investment.maturity_date
        )

        binding.tvProjected3m.text = calculateProjected(invested, 3)
        binding.tvProjected6m.text = calculateProjected(invested, 6)
        binding.tvProjected12m.text = calculateProjected(invested, 12)
        binding.tvProjectedMaturity.text = calculateProjected(invested, monthsToMaturity)

        // Product Details
        // Product Details - Use what we have
        binding.tvCategory.text = investment.product?.category?.name ?: "N/A"
        binding.tvRiskLevel.text = investment.product?.risk_level_label ?: "N/A"

        if (invested > 0 && current > invested) {
            val actualReturn = ((current - invested) / invested) * 100
            binding.tvExpectedReturns.text = String.format("%.2f%% actual", actualReturn)
        } else {
            binding.tvExpectedReturns.text = "N/A"
        }
    }

    private fun formatAmount(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        formatter.maximumFractionDigits = 2
        return formatter.format(amount)
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString)

            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    private fun calculateProgress(purchaseDate: String, maturityDate: String): Float {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")

            val purchase = format.parse(purchaseDate) ?: return 0f
            val maturity = format.parse(maturityDate) ?: return 0f
            val now = Date()

            val totalDays = TimeUnit.MILLISECONDS.toDays(maturity.time - purchase.time)
            val elapsedDays = TimeUnit.MILLISECONDS.toDays(now.time - purchase.time)

            if (totalDays <= 0) return 0f
            return (elapsedDays.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)
        } catch (e: Exception) {
            0f
        }
    }

    private fun calculateDaysRemaining(maturityDate: String): Int {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")

            val maturity = format.parse(maturityDate) ?: return 0
            val now = Date()

            TimeUnit.MILLISECONDS.toDays(maturity.time - now.time).toInt()
        } catch (e: Exception) {
            0
        }
    }

    private fun calculateMonthsToMaturity(purchaseDate: String, maturityDate: String?): Int {
        if (maturityDate == null) return 12

        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")

            val purchase = format.parse(purchaseDate) ?: return 12
            val maturity = format.parse(maturityDate) ?: return 12

            val calendar1 = Calendar.getInstance().apply { time = purchase }
            val calendar2 = Calendar.getInstance().apply { time = maturity }

            val yearsDiff = calendar2.get(Calendar.YEAR) - calendar1.get(Calendar.YEAR)
            val monthsDiff = calendar2.get(Calendar.MONTH) - calendar1.get(Calendar.MONTH)

            (yearsDiff * 12 + monthsDiff).coerceAtLeast(1)
        } catch (e: Exception) {
            12
        }
    }

    private fun calculateProjected(invested: Double, months: Int): String {
        // Use returns_percentage from UserInvestment instead
        val annualRate = investment.returns_percentage
        if (annualRate == 0.0) {
            return "N/A"
        }

        val monthlyRate = annualRate / 12 / 100
        val projected = invested * monthlyRate * months

        return "+${formatAmount(projected)} ${investment.currency}"
    }

    private fun Int.adjustAlpha(factor: Float): Int {
        val alpha = (255 * factor).toInt()
        val red = android.graphics.Color.red(this)
        val green = android.graphics.Color.green(this)
        val blue = android.graphics.Color.blue(this)
        return android.graphics.Color.argb(alpha, red, green, blue)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}