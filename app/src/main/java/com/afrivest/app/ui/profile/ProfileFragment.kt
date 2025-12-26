package com.afrivest.app.ui.profile

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
import com.afrivest.app.R
import com.afrivest.app.databinding.FragmentProfileBinding
import com.afrivest.app.ui.auth.LoginActivity
import com.afrivest.app.utils.gone
import com.afrivest.app.utils.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

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

        // Configure Account Section Rows
        with(binding.rowPersonalInfo) {
            ivIcon.setImageResource(R.drawable.ic_user_placeholder)
            tvTitle.text = "Personal Information"
            tvSubtitle.text = "Name, Email, Phone"
            root.setOnClickListener {
                // TODO: Navigate to edit profile
            }
        }

        with(binding.rowNotifications) {
            ivIcon.setImageResource(R.drawable.ic_bell)
            tvTitle.text = "Notifications"
            tvSubtitle.text = "Push, Email, SMS preferences"
            root.setOnClickListener {
                // TODO: Navigate to notifications settings
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
                viewModel.logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
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