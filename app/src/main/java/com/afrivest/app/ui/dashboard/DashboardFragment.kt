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
import android.content.Intent
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.afrivest.app.ui.dashboard.adapters.*
import timber.log.Timber
import com.afrivest.app.R
import com.afrivest.app.data.model.Wallet
import com.afrivest.app.data.model.InvestmentSummary
import com.afrivest.app.ui.deposit.DepositActivity
import com.afrivest.app.ui.insurance.InsuranceListActivity
import com.afrivest.app.ui.investments.InvestmentProductsActivity
import com.afrivest.app.ui.marketplace.GoldMarketplaceActivity
import com.afrivest.app.ui.assets.AssetsFragment
import com.google.android.material.button.MaterialButton
import androidx.core.content.ContextCompat
import com.afrivest.app.ui.transfer.WithdrawActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()
    @Inject
    lateinit var securePreferences: SecurePreferences

    private var isBalanceHidden = false
    private var isInvestmentHidden = false

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
        setupWalletCardHorizontal()
        setupObservers()
        setupClickListeners()

        // Load data
        viewModel.loadDashboard()
        // Setup RecyclerViews
        setupQuickActions()
        setupInvestments()
        setupContacts()
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

    private fun setupObservers() {
        // Loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.loadingOverlay.root.visible()
            } else {
                binding.loadingOverlay.root.gone()
            }
        }

        // Error message
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        // Profile data
        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.tvUserName.text = it.name

                // Handle avatar
                if (it.isDefaultAvatar()) {
                    // Show initials
                    binding.ivUserAvatar.visibility = View.GONE
                    binding.vAvatarBackground.visibility = View.VISIBLE
                    binding.tvUserInitial.visibility = View.VISIBLE
                    binding.tvUserInitial.text = it.getUserInitials()
                } else {
                    // Load avatar image with Glide
                    binding.vAvatarBackground.visibility = View.GONE
                    binding.tvUserInitial.visibility = View.GONE
                    binding.ivUserAvatar.visibility = View.VISIBLE

                    com.bumptech.glide.Glide.with(requireContext())
                        .load(it.avatarUrl)
                        .placeholder(R.drawable.ic_user_placeholder)
                        .error(R.drawable.ic_user_placeholder)
                        .circleCrop()
                        .into(binding.ivUserAvatar)
                }

                // Update wallet card
                updateWalletCardHorizontal(it.investmentSummary)
            }
        }

        // Greeting
        viewModel.greeting.observe(viewLifecycleOwner) { greeting ->
            binding.tvGreeting.text = greeting
        }

        // Wallets
        viewModel.wallets.observe(viewLifecycleOwner) { wallets ->
            updateWalletBalance()
        }

        // Amount visibility - REMOVED (now using local state)



        // Recent transactions
        viewModel.recentTransactions.observe(viewLifecycleOwner) { transactions ->
            // TODO: Setup transactions RecyclerView
        }
    }

    private fun setupWalletCardHorizontal() {
        val card = binding.walletCardHorizontal.root

        // Setup wallet balance section
        val tvBalance = card.findViewById<TextView>(R.id.tvBalance)
        val ivHideToggle = card.findViewById<ImageView>(R.id.ivHideToggle)
        val btnAddMoney = card.findViewById<MaterialButton>(R.id.btnAddMoney)
        val btnWithdraw = card.findViewById<MaterialButton>(R.id.btnWithdraw)

        // Setup investment section
        val ivInvestmentHideToggle = card.findViewById<ImageView>(R.id.ivInvestmentHideToggle)
        val llInvestmentSection = card.findViewById<LinearLayout>(R.id.llInvestmentSection)

        // Toggle visibility for balance only
        ivHideToggle.setOnClickListener {
            isBalanceHidden = !isBalanceHidden
            val depositWallet = viewModel.getDepositWallet()
            if (depositWallet != null) {
                tvBalance.text = if (isBalanceHidden) {
                    "****"
                } else {
                    viewModel.formatBalance(depositWallet.balance, depositWallet.currency)
                }
                ivHideToggle.setImageResource(if (isBalanceHidden) R.drawable.ic_eye_off else R.drawable.ic_eye)
            }
        }

        // Toggle visibility for investment only
        ivInvestmentHideToggle.setOnClickListener {
            isInvestmentHidden = !isInvestmentHidden
            val summary = viewModel.getInvestmentSummary()
            if (summary != null && summary.hasInvestments()) {
                val tvInvestmentAmount = card.findViewById<TextView>(R.id.tvInvestmentAmount)
                val tvReturnsPercentage = card.findViewById<TextView>(R.id.tvReturnsPercentage)

                tvInvestmentAmount.text = if (isInvestmentHidden) {
                    "****"
                } else {
                    viewModel.formatBalance(summary.currentValue.toString(), "UGX")
                }

                tvReturnsPercentage.text = if (isInvestmentHidden) {
                    "**%"
                } else {
                    summary.getFormattedPercentage()
                }

                ivInvestmentHideToggle.setImageResource(if (isInvestmentHidden) R.drawable.ic_eye_off else R.drawable.ic_eye)
            }
        }

        // Add money button
        btnAddMoney.setOnClickListener {
            startActivity(Intent(requireContext(), DepositActivity::class.java))
        }

        // Withdraw button
        btnWithdraw.setOnClickListener {
            startActivity(Intent(context, WithdrawActivity::class.java))
        }

        // Investment section click
        llInvestmentSection.setOnClickListener {
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation)
            bottomNav?.selectedItemId = R.id.nav_assets
        }
    }

    private fun updateWalletCardHorizontal(investmentSummary: InvestmentSummary?) {
        val card = binding.walletCardHorizontal.root

        val tvInvestmentAmount = card.findViewById<TextView>(R.id.tvInvestmentAmount)
        val tvReturnsPercentage = card.findViewById<TextView>(R.id.tvReturnsPercentage)

        if (investmentSummary != null && investmentSummary.hasInvestments()) {
            tvInvestmentAmount.text = viewModel.formatBalance(
                investmentSummary.currentValue.toString(),
                "UGX"
            )
            tvReturnsPercentage.text = "${investmentSummary.getFormattedPercentage()}"
        } else {
            tvInvestmentAmount.text = "UGX 0.00"
            tvReturnsPercentage.text = "0%"
        }
    }

    private fun showKYCComingSoon() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("KYC Verification")
            .setMessage("KYC verification feature coming soon")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun setupClickListeners() {
        // Header icons
        binding.btnBookmark.setOnClickListener {
            // TODO: Navigate to bookmarks
            Toast.makeText(requireContext(), "Bookmarks", Toast.LENGTH_SHORT).show()
        }

        binding.btnNotification.setOnClickListener {
            // TODO: Navigate to notifications
            Toast.makeText(requireContext(), "Notifications", Toast.LENGTH_SHORT).show()
        }

    }

    private fun updateWalletBalance() {
        val depositWallet = viewModel.getDepositWallet()
        if (depositWallet != null) {
            val card = binding.walletCardHorizontal.root
            val tvBalance = card.findViewById<TextView>(R.id.tvBalance)
            tvBalance.text = viewModel.formatBalance(depositWallet.balance, depositWallet.currency)
        }
    }

    // REMOVED - No longer needed, using local state for toggles

    private fun setupQuickActions() {
        val actions = listOf(
            QuickAction(R.drawable.ic_chart, "Invest", "invest"),
            QuickAction(R.drawable.ic_insurance, "Insurance", "insurance"),
            QuickAction(R.drawable.ic_shopping, "Marketplace", "marketplace"),
            QuickAction(R.drawable.ic_crypto, "Crypto", "crypto")
        )

        val adapter = QuickActionsAdapter(actions) { action ->
            when (action.key) {
                "invest" -> startActivity(Intent(requireContext(), InvestmentProductsActivity::class.java))
                "insurance" -> startActivity(Intent(requireContext(), InsuranceListActivity::class.java))
                "marketplace" -> startActivity(Intent(requireContext(), GoldMarketplaceActivity::class.java))
                "crypto" -> Toast.makeText(requireContext(), "Crypto - Coming Soon", Toast.LENGTH_SHORT).show()
            }
        }

        binding.rvQuickActions.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            this.adapter = adapter
        }
    }

    private fun setupInvestments() {
        viewModel.featuredInvestments.observe(viewLifecycleOwner) { investments ->
            if (investments.isNotEmpty()) {
                val adapter = InvestmentsAdapter(investments) { product ->
                    val intent = Intent(requireContext(), com.afrivest.app.ui.investments.ProductDetailActivity::class.java).apply {
                        putExtra(com.afrivest.app.ui.investments.ProductDetailActivity.EXTRA_PRODUCT, product)
                    }
                    startActivity(intent)
                }

                binding.rvInvestments.apply {
                    layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    this.adapter = adapter
                }
            }
        }
    }

    private fun setupContacts() {
        val contacts = listOf(
            Contact("Jo N", "JN", android.graphics.Color.parseColor("#2196F3")),
            Contact("Eric", "E", android.graphics.Color.parseColor("#FF9800")),
            Contact("John", "J", android.graphics.Color.parseColor("#4CAF50")),
            Contact("Doe", "D", android.graphics.Color.parseColor("#E91E63")),
            Contact("Kim", "K", android.graphics.Color.parseColor("#F44336")),
            Contact("Stella", "S", android.graphics.Color.parseColor("#9C27B0"))
        )

        val adapter = ContactsAdapter(contacts) { contact ->
            Timber.d("Contact clicked: ${contact.name}")
            Toast.makeText(requireContext(), "Send money to ${contact.name} - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        binding.rvContacts.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            this.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}