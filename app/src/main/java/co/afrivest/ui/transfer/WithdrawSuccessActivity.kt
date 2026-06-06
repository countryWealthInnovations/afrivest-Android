package co.afrivest.ui.transfer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import co.afrivest.data.model.WithdrawResponse
import co.afrivest.databinding.ActivityWithdrawSuccessBinding
import co.afrivest.ui.base.BaseActivity
import co.afrivest.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WithdrawSuccessActivity : BaseActivity() {

    private lateinit var binding: ActivityWithdrawSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWithdrawSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val withdrawResponse = intent.getParcelableExtra<WithdrawResponse>("WITHDRAW_RESPONSE")
        val phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: ""

        setupUI(withdrawResponse, phoneNumber)
        setupListeners()
    }

    private fun setupUI(response: WithdrawResponse?, phoneNumber: String) {
        response?.let {
            binding.tvReference.text = it.reference
            binding.tvAmount.text = "${it.amount} ${it.currency}"
            binding.tvPhoneNumber.text = phoneNumber
        }
    }

    private fun setupListeners() {
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

    override fun onBackPressed() {
        super.onBackPressed()
        navigateToDashboard()
    }
}