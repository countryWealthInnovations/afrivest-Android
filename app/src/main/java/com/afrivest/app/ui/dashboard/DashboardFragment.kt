package com.afrivest.app.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.afrivest.app.databinding.FragmentDashboardBinding
import com.afrivest.app.data.local.SecurePreferences
import com.afrivest.app.utils.gone
import com.afrivest.app.utils.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var securePreferences: SecurePreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupKYCBanner()
    }

    private fun setupKYCBanner() {
        if (!securePreferences.isKYCVerified()) {
            binding.kycBanner.root.visible()
            binding.kycBanner.btnCompleteKYC.setOnClickListener {
                showKYCComingSoon()
            }
        } else {
            binding.kycBanner.root.gone()
        }
    }

    private fun showKYCComingSoon() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("KYC Verification")
            .setMessage("KYC verification feature coming soon")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}