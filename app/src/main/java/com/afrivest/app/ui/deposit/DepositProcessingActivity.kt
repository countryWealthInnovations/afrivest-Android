package com.afrivest.app.ui.deposit

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.afrivest.app.databinding.ActivityDepositProcessingBinding
import com.afrivest.app.ui.main.MainActivity

class DepositProcessingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDepositProcessingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepositProcessingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val reference = intent.getStringExtra("REFERENCE") ?: ""
        val amount = intent.getStringExtra("AMOUNT") ?: ""
        val currency = intent.getStringExtra("CURRENCY") ?: ""

        setupUI(reference, amount, currency)
    }

    private fun setupUI(reference: String, amount: String, currency: String) {
        binding.textReference.text = reference
        binding.textAmount.text = "$amount $currency"

        binding.btnDone.setOnClickListener {
            navigateToDashboard()
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Allow back press to go to dashboard
        navigateToDashboard()
    }
}