package com.afrivest.app.ui.assets

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.afrivest.app.R
import com.afrivest.app.data.api.InsurancePolicy
import com.afrivest.app.databinding.ActivityFileClaimBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class FileClaimActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFileClaimBinding
    private val viewModel: FileClaimViewModel by viewModels()
    private lateinit var policy: InsurancePolicy
    private var selectedDate = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileClaimBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        policy = intent.getParcelableExtra("policy") ?: run {
            finish()
            return
        }

        setupToolbar()
        setupClaimTypeSpinner()
        setupObservers()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "File Claim"
        }
    }

    private fun setupClaimTypeSpinner() {
        val claimTypes = listOf("Medical", "Accident", "Property Damage", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, claimTypes)
        binding.actvClaimType.setAdapter(adapter)
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.loadingOverlay.root.visibility = View.VISIBLE
                binding.btnSubmit.isEnabled = false
            } else {
                binding.loadingOverlay.root.visibility = View.GONE
                binding.btnSubmit.isEnabled = true
            }
        }

        viewModel.success.observe(this) { success ->
            if (success) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Success")
                    .setMessage("Your claim has been filed successfully!")
                    .setPositiveButton("OK") { _, _ ->
                        setResult(RESULT_OK)
                        finish()
                    }
                    .setCancelable(false)
                    .show()
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
        binding.tvIncidentDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnSubmit.setOnClickListener {
            submitClaim()
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                updateDateDisplay()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateDisplay() {
        val format = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        binding.tvIncidentDate.text = format.format(selectedDate.time)
    }

    private fun submitClaim() {
        val claimType = binding.actvClaimType.text.toString()
        val amount = binding.etAmount.text.toString()
        val description = binding.etDescription.text.toString()

        // Validation
        if (claimType.isEmpty()) {
            binding.tilClaimType.error = "Select claim type"
            return
        } else {
            binding.tilClaimType.error = null
        }

        if (amount.isEmpty()) {
            binding.tilAmount.error = "Enter amount"
            return
        } else {
            binding.tilAmount.error = null
        }

        if (description.isEmpty()) {
            binding.tilDescription.error = "Enter description"
            return
        } else {
            binding.tilDescription.error = null
        }

        val incidentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedDate.time)

        viewModel.fileClaim(policy.id, claimType, amount, description, incidentDate)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}