package co.afrivest.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.net.Uri
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import co.afrivest.R
import co.afrivest.data.local.SecurePreferences
import co.afrivest.data.repository.AuthRepository
import co.afrivest.databinding.FragmentProfileBinding
import co.afrivest.ui.auth.LoginActivity
import co.afrivest.utils.gone
import co.afrivest.utils.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    @Inject
    lateinit var securePreferences: SecurePreferences

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var preferencesManager: co.afrivest.data.local.PreferencesManager

    @Inject
    lateinit var loanRepository: co.afrivest.data.repository.LoanRepository

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBiometric()
        setupUI()
        observeViewModel()
        viewModel.loadProfile()
    }

    override fun onResume() {
        super.onResume()
        // Refresh currency subtitle in case user changed it
        binding.rowCurrency.tvSubtitle.text = preferencesManager.defaultCurrency ?: "Not set"
    }

    private fun setupBiometric() {
        val executor = ContextCompat.getMainExecutor(requireContext())

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Timber.e("Biometric error: $errString")
                    binding.switchBiometric.isChecked = false
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.enableBiometric(true)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Timber.e("Biometric authentication failed")
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Enable Biometric Authentication")
            .setSubtitle("Authenticate to enable biometric login")
            .setNegativeButtonText("Cancel")
            .build()
    }

    private fun setupUI() {
        // Profile Header
        binding.tvTitle.text = "Profile & Settings"

        // Logout Button
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        // Delete Account Button
        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmation()
        }

        // Configure Account Section Rows
        with(binding.rowPersonalInfo) {
            ivIcon.setImageResource(R.drawable.ic_user_placeholder)
            tvTitle.text = "Personal Information"
            tvSubtitle.text = "Name, Email, Phone"
            root.setOnClickListener {
                startActivity(Intent(requireContext(), EditProfileActivity::class.java))
            }
        }

        with(binding.rowNotifications) {
            ivIcon.setImageResource(R.drawable.ic_bell)
            tvTitle.text = "Notifications"
            tvSubtitle.text = "Push, Email, SMS preferences"
            root.setOnClickListener {
                startActivity(Intent(requireContext(), NotificationSettingsActivity::class.java))
            }
        }

        with(binding.rowCurrency) {
            ivIcon.setImageResource(R.drawable.ic_currency)
            tvTitle.text = "Currency"
            tvSubtitle.text = preferencesManager.defaultCurrency ?: "Not set"
            root.setOnClickListener {
                startActivity(Intent(requireContext(), co.afrivest.ui.onboarding.CurrencySelectionActivity::class.java))
            }
        }
        with(binding.rowAllocation) {
            ivIcon.setImageResource(R.drawable.ic_chart)
            tvTitle.text = "Deposit Allocation"
            tvSubtitle.text = "Split each deposit across wallet, savings, investment"
            root.setOnClickListener {
                startActivity(Intent(requireContext(), co.afrivest.ui.allocation.AllocationSettingsActivity::class.java))
            }
        }

        with(binding.rowNextOfKin) {
            ivIcon.setImageResource(R.drawable.ic_user_placeholder)
            tvTitle.text = "Next of Kin"
            tvSubtitle.text = "Who to contact in an emergency"
            root.setOnClickListener {
                startActivity(Intent(requireContext(), co.afrivest.ui.profile.NextOfKinActivity::class.java))
            }
        }

        // Configure Security Section Rows
        with(binding.rowChangePassword) {
            ivIcon.setImageResource(R.drawable.ic_lock)
            tvTitle.text = "Change Password"
            tvSubtitle.text = "Update your account password"
            root.setOnClickListener {
                startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
            }
        }

        // Configure Support Section Rows
        with(binding.rowHelpCenter) {
            ivIcon.setImageResource(R.drawable.ic_help)
            tvTitle.text = "Help Center"
            tvSubtitle.text = "FAQs and support articles"
            root.setOnClickListener {
                showHelpCenterBottomSheet()
            }
        }

        with(binding.rowTerms) {
            ivIcon.setImageResource(R.drawable.ic_document)
            tvTitle.text = "Terms & Conditions"
            tvSubtitle.text = "Legal agreements"
            root.setOnClickListener {
                openUrl("https://afrivest.co/terms")
            }
        }

        with(binding.rowPrivacy) {
            ivIcon.setImageResource(R.drawable.ic_shield)
            tvTitle.text = "Privacy Policy"
            tvSubtitle.text = "How we handle your data"
            root.setOnClickListener {
                openUrl("https://afrivest.co/policy")
            }
        }

        // Rest of setup...
    }

    private fun observeViewModel() {
        // User
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvUserName.text = it.name
                binding.tvUserEmail.text = it.email
                binding.tvUserPhone.text = it.phone_number ?: "No phone number"

                // Set initials avatar
                val initials = getInitials(it.name)
                binding.tvInitials.text = initials

                renderStatus(it)
                if (it.role.equals("advisor", true)) {
                    binding.cardAdvisor.visible()
                    binding.btnAdvisorDashboard.setOnClickListener {
                        startActivity(Intent(requireContext(), co.afrivest.ui.advisors.AdvisorDashboardActivity::class.java))
                    }
                } else {
                    binding.cardAdvisor.gone()
                }
            }
        }

        // Biometric Enabled
        viewModel.biometricEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.switchBiometric.isChecked = enabled
        }

        // Loading
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.loadingOverlay.root.visible()
            } else {
                binding.loadingOverlay.root.gone()
            }
        }

        // Error
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Error")
                    .setMessage(it)
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        // Logout Success
        viewModel.logoutSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                navigateToLogin()
            }
        }
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteAccountConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logout() {
        lifecycleScope.launch {
            try {
                // Call logout API
                authRepository.logout()
                // Navigate to login screen (data already cleared in repository)
                navigateToLogin()
            } catch (e: Exception) {
                Timber.e(e, "Logout API failed")
                // Even if API fails, clear local data and logout
                securePreferences.clearAll()
                navigateToLogin()
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun renderStatus(user: co.afrivest.data.model.User) {
        val profile = securePreferences.getCachedProfile()
        renderSummary(profile)

        applyStatusRow(
            binding.rowStatusEmail, "Email", user.email_verified,
            verifiedText = "Verified", pendingText = "Not verified"
        )

        val phoneVerified = securePreferences.isPhoneVerified()
        applyStatusRow(
            binding.rowStatusPhone, "Phone", phoneVerified,
            verifiedText = "Verified", pendingText = "Verify now"
        )
        binding.rowStatusPhone.root.setOnClickListener {
            if (!phoneVerified) {
                startActivity(
                    Intent(requireContext(), co.afrivest.ui.auth.PhoneOTPActivity::class.java)
                        .putExtra("phone", user.phone_number)
                )
            }
        }

        val kycVerified = user.kyc_verified || (profile?.kycVerified == true)
        applyStatusRow(
            binding.rowStatusKyc, "Identity (KYC)", kycVerified,
            verifiedText = "Verified", pendingText = "Verify now"
        )
        binding.rowStatusKyc.root.setOnClickListener {
            if (!kycVerified) {
                startActivity(Intent(requireContext(), co.afrivest.ui.kyc.KycActivity::class.java))
            }
        }

        applyStatusRow(
            binding.rowStatusAccount, "Account",
            user.status.equals("active", true),
            verifiedText = "Active", pendingText = user.status.replaceFirstChar { it.uppercase() }
        )
    }

    private fun renderSummary(profile: co.afrivest.data.model.ProfileData?) {
        val currency = preferencesManager.defaultCurrency ?: "UGX"
        val walletBalance = profile?.wallets?.firstOrNull { it.currency == currency }?.balance
            ?: profile?.wallets?.firstOrNull()?.balance ?: "0"
        binding.tvSummaryWallet.text = "$currency $walletBalance"

        val portfolio = profile?.investmentSummary?.currentValue ?: 0.0
        binding.tvSummaryPortfolio.text = String.format("%,.0f", portfolio)

        binding.tvSummaryLoan.text = "…"
        viewLifecycleOwner.lifecycleScope.launch {
            when (val r = loanRepository.getMyLoans()) {
                is co.afrivest.data.model.Resource.Success -> {
                    val borrowed = r.data?.borrowed ?: emptyList()
                    val active = borrowed.filter { it.status in listOf("active", "funded", "overdue") }
                    val userCurrency = preferencesManager.defaultCurrency ?: "UGX"
                    var total = 0.0
                    for (l in active) {
                        val raw = (l.outstanding as? Number)?.toDouble()
                            ?: (l.outstanding as? String)?.toDoubleOrNull()
                            ?: 0.0
                        total += co.afrivest.utils.FeeCalculator.convertCurrency(
                            raw, from = l.currency ?: userCurrency, to = userCurrency,
                            preferencesManager = preferencesManager
                        )
                    }
                    binding.tvSummaryLoan.text = if (total > 0.0) "$userCurrency ${String.format("%,.0f", total)}" else "None"
                }
                is co.afrivest.data.model.Resource.Error -> binding.tvSummaryLoan.text = "None"
                is co.afrivest.data.model.Resource.Loading -> {}
            }
        }
    }

    private fun applyStatusRow(
        row: co.afrivest.databinding.ItemStatusRowBinding,
        label: String,
        ok: Boolean,
        verifiedText: String,
        pendingText: String
    ) {
        row.tvStatusLabel.text = label
        row.tvStatusValue.text = if (ok) verifiedText else pendingText
        val color = if (ok) android.graphics.Color.parseColor("#10b981")
                    else android.graphics.Color.parseColor("#EFBF04")
        row.tvStatusValue.setTextColor(color)
        row.ivStatusIcon.setImageResource(
            if (ok) R.drawable.ic_check_circle else R.drawable.ic_shield
        )
        row.ivStatusIcon.setColorFilter(color)
    }

    private fun getInitials(name: String): String {
        val parts = name.trim().split(" ")
        return if (parts.size >= 2) {
            "${parts[0].first()}${parts[1].first()}".uppercase()
        } else {
            name.take(1).uppercase()
        }
    }

    private fun showHelpCenterBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_help_center, null)

        val btnEmail = view.findViewById<LinearLayout>(R.id.btn_email)
        val btnPhone = view.findViewById<LinearLayout>(R.id.btn_phone)

        btnEmail.setOnClickListener {
            bottomSheetDialog.dismiss()
            openEmail()
        }

        btnPhone.setOnClickListener {
            bottomSheetDialog.dismiss()
            openPhone()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    private fun openEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:hello@afrivest.co")
        }
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun openPhone() {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:+256700000000") // Replace with actual phone number
        }
        startActivity(intent)
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}