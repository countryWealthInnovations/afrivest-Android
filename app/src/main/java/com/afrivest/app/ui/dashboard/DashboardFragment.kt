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
import com.afrivest.app.ui.deposit.DepositActivity
import com.afrivest.app.ui.insurance.InsuranceListActivity
import com.afrivest.app.ui.investments.InvestmentCategoriesActivity
import com.afrivest.app.ui.investments.InvestmentProductsActivity
import com.afrivest.app.ui.marketplace.GoldMarketplaceActivity
import com.afrivest.app.ui.marketplace.MarketplaceActivity
import com.google.android.material.button.MaterialButton


@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()
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
            }
        }

        // Greeting
        viewModel.greeting.observe(viewLifecycleOwner) { greeting ->
            binding.tvGreeting.text = greeting
        }

        // Wallets
        viewModel.wallets.observe(viewLifecycleOwner) { wallets ->
            setupWalletCards()
        }

        // Amount visibility
        viewModel.isAmountHidden.observe(viewLifecycleOwner) { isHidden ->
            updateWalletCardsVisibility(isHidden)
        }

        // Other currencies expansion
        viewModel.isOtherCurrenciesExpanded.observe(viewLifecycleOwner) { isExpanded ->
            if (isExpanded) {
                binding.llOtherCurrencies.visible()
                binding.btnToggleOtherCurrencies.text = "Hide Other Currencies"
                binding.btnToggleOtherCurrencies.setIconResource(R.drawable.ic_chevron_up)
                setupOtherCurrencyCards()
            } else {
                binding.llOtherCurrencies.gone()
                binding.btnToggleOtherCurrencies.text = "Show Other Currencies"
                binding.btnToggleOtherCurrencies.setIconResource(R.drawable.ic_chevron_down)
            }
        }

        // Recent transactions
        viewModel.recentTransactions.observe(viewLifecycleOwner) { transactions ->
            // TODO: Setup transactions RecyclerView
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

        // Toggle other currencies
        binding.btnToggleOtherCurrencies.setOnClickListener {
            viewModel.toggleOtherCurrencies()
        }

        // Quick Actions

    }

    private fun setupWalletCards() {
        // Setup Deposit Wallet Card
        val depositWallet = viewModel.getDepositWallet()
        if (depositWallet != null) {
            binding.depositWalletCard.root.visible()
            setupDepositWalletCard(depositWallet)
        } else {
            binding.depositWalletCard.root.gone()
        }

        // Setup Interest Wallet Card - Show placeholder (pass null or dummy wallet)
        val interestWallet = viewModel.getInterestWallet()
        if (interestWallet != null) {
            setupInterestWalletCard(interestWallet)
        } else {
            // Show placeholder - create a dummy wallet or just call the setup function
            binding.interestWalletCard.root.visible()
            setupInterestWalletPlaceholder()
        }

        // Show/hide toggle button based on other currencies
        if (viewModel.getOtherCurrencyWallets().isEmpty()) {
            binding.btnToggleOtherCurrencies.gone()
        } else {
            binding.btnToggleOtherCurrencies.visible()
            binding.btnToggleOtherCurrencies.text = "Show Other Currencies (${viewModel.getOtherCurrencyWallets().size})"
        }
    }

    private fun setupInterestWalletPlaceholder() {
        val cardView = binding.interestWalletCard.root

        // Make card slightly transparent to indicate it's a placeholder
        cardView.alpha = 0.5f

        // Get views from included layout
        val ivWalletIcon = cardView.findViewById<ImageView>(R.id.ivWalletIcon)
        val tvWalletTitle = cardView.findViewById<TextView>(R.id.tvWalletTitle)
        val ivHideToggle = cardView.findViewById<ImageView>(R.id.ivHideToggle)
        val tvBalance = cardView.findViewById<TextView>(R.id.tvBalance)
        val btnAction = cardView.findViewById<MaterialButton>(R.id.btnAction)

        // Set wallet icon with reduced opacity
        ivWalletIcon.setImageResource(R.drawable.ic_chart)
        ivWalletIcon.setColorFilter(
            resources.getColor(R.color.primary_gold, null),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        ivWalletIcon.alpha = 0.5f

        // Set title
        tvWalletTitle.text = "Interest Wallet"
        tvWalletTitle.setTextColor(resources.getColor(R.color.text_secondary, null))

        // Hide toggle button
        ivHideToggle.visibility = View.GONE

        // Set placeholder balance
        tvBalance.text = "Coming Soon"
        tvBalance.setTextColor(resources.getColor(R.color.text_secondary, null))
        tvBalance.alpha = 0.5f

        // Set action button as disabled
        btnAction.text = "Start Earning"
        btnAction.isEnabled = false
        btnAction.alpha = 0.3f
        btnAction.icon = null
        btnAction.setOnClickListener(null)
    }

    private fun setupDepositWalletCard(wallet: com.afrivest.app.data.model.Wallet) {
        val cardView = binding.depositWalletCard.root

        // Get views from included layout
        val ivWalletIcon = cardView.findViewById<ImageView>(R.id.ivWalletIcon)
        val tvWalletTitle = cardView.findViewById<TextView>(R.id.tvWalletTitle)
        val ivHideToggle = cardView.findViewById<ImageView>(R.id.ivHideToggle)
        val tvBalance = cardView.findViewById<TextView>(R.id.tvBalance)
        val btnAction = cardView.findViewById<MaterialButton>(R.id.btnAction)

        // Set wallet icon (deposit wallet)
        ivWalletIcon.setImageResource(R.drawable.ic_wallet)

        // Set title
        tvWalletTitle.text = "Deposit Wallet"

        // Set balance
        tvBalance.text = viewModel.formatBalance(wallet.balance, wallet.currency)

        // Set action button
        btnAction.text = "Deposit"
        btnAction.setIconResource(R.drawable.ic_chevron_down)
        btnAction.setOnClickListener {
            startActivity(Intent(requireContext(), DepositActivity::class.java))
        }

        // Toggle visibility
        ivHideToggle.setOnClickListener {
            viewModel.toggleAmountVisibility()
        }
    }

    private fun setupInterestWalletCard(wallet: com.afrivest.app.data.model.Wallet) {
        val cardView = binding.interestWalletCard.root
        cardView.visible()

        // Make card slightly transparent to indicate it's a placeholder
        cardView.alpha = 0.5f

        // Get views from included layout
        val ivWalletIcon = cardView.findViewById<ImageView>(R.id.ivWalletIcon)
        val tvWalletTitle = cardView.findViewById<TextView>(R.id.tvWalletTitle)
        val ivHideToggle = cardView.findViewById<ImageView>(R.id.ivHideToggle)
        val tvBalance = cardView.findViewById<TextView>(R.id.tvBalance)
        val btnAction = cardView.findViewById<MaterialButton>(R.id.btnAction)

        // Set wallet icon with reduced opacity
        ivWalletIcon.setImageResource(R.drawable.ic_chart)
        ivWalletIcon.setColorFilter(
            resources.getColor(R.color.primary_gold, null),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        ivWalletIcon.alpha = 0.5f

        // Set title
        tvWalletTitle.text = "Interest Wallet"
        tvWalletTitle.setTextColor(resources.getColor(R.color.text_secondary, null))

        // Hide toggle button
        ivHideToggle.visibility = View.GONE

        // Set placeholder balance
        tvBalance.text = "Coming Soon"
        tvBalance.setTextColor(resources.getColor(R.color.text_secondary, null))
        tvBalance.alpha = 0.5f

        // Set action button as disabled with gray background
        btnAction.text = "Start Earning"
        btnAction.isEnabled = false
        btnAction.setBackgroundColor(resources.getColor(R.color.text_secondary, null))
        btnAction.alpha = 0.3f
        btnAction.icon = null
        btnAction.setOnClickListener(null)
    }

//    private fun setupInterestWalletCard(wallet: com.afrivest.app.data.model.Wallet) {
//        val cardView = binding.interestWalletCard.root
//
//        // Get views from included layout
//        val ivWalletIcon = cardView.findViewById<ImageView>(R.id.ivWalletIcon)
//        val tvWalletTitle = cardView.findViewById<TextView>(R.id.tvWalletTitle)
//        val ivHideToggle = cardView.findViewById<ImageView>(R.id.ivHideToggle)
//        val tvBalance = cardView.findViewById<TextView>(R.id.tvBalance)
//        val btnAction = cardView.findViewById<MaterialButton>(R.id.btnAction)
//
//        // Set wallet icon (interest wallet)
//        ivWalletIcon.setImageResource(R.drawable.ic_chart)
//
//        // Set title
//        tvWalletTitle.text = "Interest Wallet"
//
//        // Set balance
//        tvBalance.text = viewModel.formatBalance(wallet.balance, wallet.currency)
//
//        // Set action button
//        btnAction.text = "Withdraw"
//        btnAction.setIconResource(R.drawable.ic_chevron_up)
//        btnAction.setOnClickListener {
//            // TODO: Navigate to withdraw screen
//            Toast.makeText(requireContext(), "Withdraw - Coming Soon", Toast.LENGTH_SHORT).show()
//        }
//
//        // Toggle visibility
//        ivHideToggle.setOnClickListener {
//            viewModel.toggleAmountVisibility()
//        }
//    }

    private fun updateWalletCardsVisibility(isHidden: Boolean) {
        // Update deposit wallet
        val depositWallet = viewModel.getDepositWallet()
        if (depositWallet != null) {
            val cardView = binding.depositWalletCard.root
            val tvBalance = cardView.findViewById<TextView>(R.id.tvBalance)
            val ivHideToggle = cardView.findViewById<ImageView>(R.id.ivHideToggle)

            tvBalance.text = viewModel.formatBalance(depositWallet.balance, depositWallet.currency)
            ivHideToggle.setImageResource(
                if (isHidden) R.drawable.ic_eye_off else R.drawable.ic_eye
            )
        }

        // Update interest wallet
        val interestWallet = viewModel.getInterestWallet()
        if (interestWallet != null) {
            val cardView = binding.interestWalletCard.root
            val tvBalance = cardView.findViewById<TextView>(R.id.tvBalance)
            val ivHideToggle = cardView.findViewById<ImageView>(R.id.ivHideToggle)

            tvBalance.text = viewModel.formatBalance(interestWallet.balance, interestWallet.currency)
            ivHideToggle.setImageResource(
                if (isHidden) R.drawable.ic_eye_off else R.drawable.ic_eye
            )
        }
    }

    private fun setupOtherCurrencyCards() {
        binding.llOtherCurrencies.removeAllViews()

        val otherWallets = viewModel.getOtherCurrencyWallets()

        if (otherWallets.isEmpty()) {
            return
        }

        // Group wallets into pairs (2 per row)
        otherWallets.chunked(2).forEach { pair ->
            // Create horizontal LinearLayout for the row
            val rowLayout = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = resources.getDimensionPixelSize(R.dimen.spacing_md)
                }
                orientation = LinearLayout.HORIZONTAL
                weightSum = 2f
            }

            // Add first wallet card
            val firstCard = createOtherCurrencyCard(pair[0])
            val firstParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                rightMargin = resources.getDimensionPixelSize(R.dimen.spacing_sm)
            }
            firstCard.layoutParams = firstParams
            rowLayout.addView(firstCard)

            // Add second wallet card or spacer
            if (pair.size > 1) {
                val secondCard = createOtherCurrencyCard(pair[1])
                val secondParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    leftMargin = resources.getDimensionPixelSize(R.dimen.spacing_sm)
                }
                secondCard.layoutParams = secondParams
                rowLayout.addView(secondCard)
            } else {
                // Add empty space for alignment
                val spacer = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }
                rowLayout.addView(spacer)
            }

            binding.llOtherCurrencies.addView(rowLayout)
        }
    }

    private fun createOtherCurrencyCard(wallet: Wallet): View {
        val cardView = layoutInflater.inflate(R.layout.item_other_currency_card, null)

        val ivCurrencyIcon = cardView.findViewById<ImageView>(R.id.ivCurrencyIcon)
        val tvCurrencyName = cardView.findViewById<TextView>(R.id.tvCurrencyName)
        val tvBalance = cardView.findViewById<TextView>(R.id.tvBalance)
        val btnViewDetails = cardView.findViewById<MaterialButton>(R.id.btnViewDetails)

        // Set currency icon based on currency type
        val iconRes = when (wallet.currency) {
            "USD" -> R.drawable.ic_dollar
            "EUR" -> R.drawable.ic_euro
            "GBP" -> R.drawable.ic_pound
            else -> R.drawable.ic_currency
        }
        ivCurrencyIcon.setImageResource(iconRes)

        // Set currency name
        tvCurrencyName.text = "${wallet.currency} Wallet"

        // Set balance
        tvBalance.text = viewModel.formatBalance(wallet.balance, wallet.currency)

        // Set click listener for view details
        btnViewDetails.setOnClickListener {
            // TODO: Navigate to wallet detail screen
            Toast.makeText(requireContext(), "${wallet.currency} Wallet Details - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        return cardView
    }

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