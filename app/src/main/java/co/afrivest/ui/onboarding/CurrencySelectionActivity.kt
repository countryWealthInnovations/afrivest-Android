package co.afrivest.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import co.afrivest.databinding.ActivityCurrencySelectionBinding
import co.afrivest.data.local.PreferencesManager
import co.afrivest.ui.base.BaseActivity
import co.afrivest.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CurrencySelectionActivity : BaseActivity() {

    private lateinit var binding: ActivityCurrencySelectionBinding
    private val viewModel: CurrencySelectionViewModel by viewModels()

    @Inject lateinit var prefs: PreferencesManager

    private val currencies = listOf(
        "UGX" to "🇺🇬 UGX — Ugandan Shilling",
        "USD" to "🇺🇸 USD — US Dollar",
        "GBP" to "🇬🇧 GBP — British Pound",
        "EUR" to "🇪🇺 EUR — Euro",
        "KES" to "🇰🇪 KES — Kenyan Shilling",
        "NGN" to "🇳🇬 NGN — Nigerian Naira",
        "ZAR" to "🇿🇦 ZAR — South African Rand",
        "CAD" to "🇨🇦 CAD — Canadian Dollar",
        "AED" to "🇦🇪 AED — UAE Dirham",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCurrencySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val labels = currencies.map { it.second }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPrimary.adapter = adapter

        val secondaryLabels = listOf("None") + labels
        val secondaryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, secondaryLabels)
        secondaryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSecondary.adapter = secondaryAdapter

        // Pre-select current currencies
        val currentPrimary = prefs.defaultCurrency
        if (currentPrimary != null) {
            val idx = currencies.indexOfFirst { it.first == currentPrimary }
            if (idx >= 0) binding.spinnerPrimary.setSelection(idx)
        }

        val currentSecondary = prefs.secondaryCurrency
        if (currentSecondary != null) {
            val idx = currencies.indexOfFirst { it.first == currentSecondary }
            if (idx >= 0) binding.spinnerSecondary.setSelection(idx + 1) // +1 for "None" at position 0
        }

        // Check email verification via SecurePreferences
        val securePrefs = co.afrivest.data.local.SecurePreferences(this)
        val isVerified = securePrefs.isEmailVerified()
        if (!isVerified) {
            binding.tvError.text = "⚠️ Please verify your email before setting your currency."
            binding.btnContinue.isEnabled = false
            binding.btnContinue.alpha = 0.5f
        }

        binding.btnContinue.setOnClickListener {
            if (!securePrefs.isEmailVerified()) {
                binding.tvError.text = "Please verify your email first."
                return@setOnClickListener
            }
            val primary  = currencies[binding.spinnerPrimary.selectedItemPosition].first
            val secIdx   = binding.spinnerSecondary.selectedItemPosition
            val secondary = if (secIdx == 0) null else currencies[secIdx - 1].first
            viewModel.saveCurrency(primary, secondary)
            prefs.defaultCurrency   = primary
            prefs.secondaryCurrency = secondary
        }

        viewModel.saved.observe(this) { success ->
            if (success) {
                prefs.defaultCurrency   = currencies[binding.spinnerPrimary.selectedItemPosition].first
                val secIdx = binding.spinnerSecondary.selectedItemPosition
                prefs.secondaryCurrency = if (secIdx == 0) null else currencies[secIdx - 1].first
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        viewModel.error.observe(this) { msg ->
            binding.tvError.text = msg ?: ""
        }
    }
}