package co.afrivest.ui.auth

import android.os.Bundle
import androidx.activity.viewModels
import co.afrivest.databinding.ActivityOtpBinding
import co.afrivest.ui.base.BaseActivity
import co.afrivest.utils.OTPBoxHandler
import co.afrivest.utils.gone
import co.afrivest.utils.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhoneOTPActivity : BaseActivity() {

    private lateinit var binding: ActivityOtpBinding
    private val viewModel: PhoneOTPViewModel by viewModels()
    private lateinit var otpHandler: OTPBoxHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val phone = intent.getStringExtra("phone") ?: ""
        viewModel.initialize(phone)

        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun setupUI() {
        val otpBoxes = listOf(
            binding.otpBox1, binding.otpBox2, binding.otpBox3,
            binding.otpBox4, binding.otpBox5, binding.otpBox6
        )
        otpHandler = OTPBoxHandler(otpBoxes) { otp -> viewModel.onOTPChanged(otp) }
    }

    private fun setupListeners() {
        binding.btnVerify.setOnClickListener { viewModel.verifyOTP() }
        binding.tvResend.setOnClickListener { viewModel.resendOTP() }
    }

    private fun observeViewModel() {
        viewModel.phone.observe(this) { binding.tvEmail.text = it }

        viewModel.timeRemaining.observe(this) { time ->
            binding.tvTimer.text = "Code expires in ${viewModel.getFormattedTime()}"
            binding.tvTimer.setTextColor(
                getColor(if (time < 60) co.afrivest.R.color.error_red else co.afrivest.R.color.text_secondary)
            )
        }

        viewModel.canResend.observe(this) { canResend ->
            binding.tvResend.isEnabled = canResend
            binding.tvResend.setTextColor(
                getColor(if (canResend) co.afrivest.R.color.primary_gold else co.afrivest.R.color.text_disabled)
            )
        }

        viewModel.otpCode.observe(this) { binding.btnVerify.isEnabled = it.length == 6 }

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) { binding.loadingOverlay.root.visible(); binding.btnVerify.isEnabled = false }
            else binding.loadingOverlay.root.gone()
        }

        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Error").setMessage(it)
                    .setPositiveButton("OK") { _, _ -> viewModel.clearError() }
                    .show()
            }
        }

        viewModel.verified.observe(this) { done ->
            if (done) { setResult(RESULT_OK); finish() }
        }
    }
}