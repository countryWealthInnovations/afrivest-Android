package com.afrivest.app.ui.assets

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.afrivest.app.R
import com.afrivest.app.data.api.InsurancePolicy
import com.afrivest.app.databinding.ActivityPolicyDetailBinding
import com.afrivest.app.ui.assets.adapters.ClaimsAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class PolicyDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPolicyDetailBinding
    private lateinit var policy: InsurancePolicy
    private val viewModel: PolicyDetailViewModel by viewModels()
    private lateinit var claimsAdapter: ClaimsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPolicyDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        policy = intent.getParcelableExtra("policy") ?: run {
            finish()
            return
        }

        setupToolbar()
        setupViews()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        viewModel.loadClaims(policy.id)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Policy Details"
        }
    }

    private fun setupViews() {
        // Header
        binding.tvPolicyNumber.text = policy.policy_number
        binding.tvPolicyType.text = policy.policy_type.replace("_", " ").uppercase()

        // Status
        binding.tvStatus.text = policy.status.uppercase()
        val statusColor = when (policy.status.lowercase()) {
            "active" -> R.color.success_green
            "expired" -> R.color.text_secondary
            "cancelled" -> R.color.error_red
            "pending" -> R.color.warning_yellow
            else -> R.color.text_secondary
        }
        binding.tvStatus.setTextColor(getColor(statusColor))
        binding.tvStatus.setBackgroundColor(getColor(statusColor).adjustAlpha(0.1f))

        // Provider
        policy.partner?.let {
            binding.tvProvider.text = "Provider: ${it.name}"
        }

        // Coverage
        binding.tvCoverage.text = policy.coverage_amount
        binding.tvPremium.text = policy.premium_amount
        binding.tvStartDate.text = formatDate(policy.start_date)
        binding.tvEndDate.text = formatDate(policy.end_date)

        // Show/hide file claim button
        if (policy.status.lowercase() == "active") {
            binding.btnFileClaim.visibility = View.VISIBLE
        } else {
            binding.btnFileClaim.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        claimsAdapter = ClaimsAdapter()

        binding.rvClaims.apply {
            layoutManager = LinearLayoutManager(this@PolicyDetailActivity)
            adapter = claimsAdapter
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingOverlay.root.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.claims.observe(this) { claims ->
            if (claims.isEmpty()) {
                binding.tvEmptyClaims.visibility = View.VISIBLE
                binding.rvClaims.visibility = View.GONE
            } else {
                binding.tvEmptyClaims.visibility = View.GONE
                binding.rvClaims.visibility = View.VISIBLE
                claimsAdapter.submitList(claims)
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Error")
                    .setMessage(it)
                    .setPositiveButton("OK", null)
                    .show()
                viewModel.clearError()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnFileClaim.setOnClickListener {
            val intent = Intent(this, FileClaimActivity::class.java).apply {
                putExtra("policy", policy)
            }
            startActivityForResult(intent, REQUEST_FILE_CLAIM)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_FILE_CLAIM && resultCode == RESULT_OK) {
            viewModel.loadClaims(policy.id)
        }
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

    companion object {
        private const val REQUEST_FILE_CLAIM = 100
    }
}