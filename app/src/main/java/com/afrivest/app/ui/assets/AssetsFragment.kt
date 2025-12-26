package com.afrivest.app.ui.assets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.afrivest.app.databinding.FragmentAssetsBinding
import com.afrivest.app.ui.assets.adapters.InvestmentsAdapter
import com.afrivest.app.ui.assets.adapters.PoliciesAdapter
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class AssetsFragment : Fragment() {

    private var _binding: FragmentAssetsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AssetsViewModel by viewModels()

    private lateinit var investmentsAdapter: InvestmentsAdapter
    private lateinit var policiesAdapter: PoliciesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupTabs()
        setupObservers()

        viewModel.loadData()
    }

    private fun setupRecyclerViews() {
        // Investments
        investmentsAdapter = InvestmentsAdapter { investment ->
            // TODO: Navigate to investment details
        }

        binding.rvInvestments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = investmentsAdapter
        }

        // Policies
        policiesAdapter = PoliciesAdapter { policy ->
            // TODO: Navigate to policy details
        }

        binding.rvPolicies.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = policiesAdapter
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Investments"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Insurance"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showInvestments()
                    1 -> showPolicies()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        showInvestments()
    }

    private fun showInvestments() {
        binding.rvInvestments.visibility = View.VISIBLE
        binding.rvPolicies.visibility = View.GONE
        binding.tvEmptyInvestments.visibility = if (viewModel.investments.value.isNullOrEmpty()) View.VISIBLE else View.GONE
    }

    private fun showPolicies() {
        binding.rvInvestments.visibility = View.GONE
        binding.rvPolicies.visibility = View.VISIBLE
        binding.tvEmptyPolicies.visibility = if (viewModel.policies.value.isNullOrEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.loadingOverlay.root.visibility = View.VISIBLE
            } else {
                binding.loadingOverlay.root.visibility = View.GONE
            }
        }

        viewModel.investments.observe(viewLifecycleOwner) { investments ->
            investmentsAdapter.submitList(investments)
            binding.tvEmptyInvestments.visibility = if (investments.isEmpty() && binding.tabLayout.selectedTabPosition == 0) {
                View.VISIBLE
            } else {
                View.GONE
            }

            updateSummary()
            updateTabCounts()
        }

        viewModel.policies.observe(viewLifecycleOwner) { policies ->
            policiesAdapter.submitList(policies)
            binding.tvEmptyPolicies.visibility = if (policies.isEmpty() && binding.tabLayout.selectedTabPosition == 1) {
                View.VISIBLE
            } else {
                View.GONE
            }

            updateTabCounts()
        }
    }

    private fun updateSummary() {
        if (viewModel.investments.value.isNullOrEmpty()) {
            binding.cardSummary.visibility = View.GONE
        } else {
            binding.cardSummary.visibility = View.VISIBLE
            binding.tvTotalValue.text = "UGX ${formatAmount(viewModel.totalInvestmentValue)}"
            binding.tvTotalReturns.text = "+UGX ${formatAmount(viewModel.totalReturns)}"
        }
    }

    private fun updateTabCounts() {
        val investmentCount = viewModel.investments.value?.size ?: 0
        val policyCount = viewModel.policies.value?.size ?: 0

        binding.tabLayout.getTabAt(0)?.text = "Investments ($investmentCount)"
        binding.tabLayout.getTabAt(1)?.text = "Insurance ($policyCount)"
    }

    private fun formatAmount(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        formatter.maximumFractionDigits = 0
        return formatter.format(amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}