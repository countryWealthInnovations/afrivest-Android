package co.afrivest.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import co.afrivest.databinding.FragmentHomeBinding
import co.afrivest.data.local.SecurePreferences
import co.afrivest.utils.gone
import co.afrivest.utils.visible
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
import co.afrivest.ui.home.adapters.*
import timber.log.Timber
import co.afrivest.R
import co.afrivest.data.model.InvestmentSummary
import co.afrivest.ui.deposit.DepositActivity
import co.afrivest.ui.insurance.InsuranceListActivity
import co.afrivest.ui.investments.InvestmentProductsActivity
import com.google.android.material.button.MaterialButton
import androidx.core.content.ContextCompat
import co.afrivest.ui.transfer.SendMoneyActivity
import co.afrivest.ui.transfer.WithdrawActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    @Inject
    lateinit var securePreferences: SecurePreferences

    @Inject
    lateinit var preferencesManager: co.afrivest.data.local.PreferencesManager

    private var isBalanceHidden = false
    private var isInvestmentHidden = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadDashboard()
        refreshKYCBanner()
    }

    private fun refreshKYCBanner() {
        if (!securePreferences.isKYCVerified()) {
            binding.kycBanner.root.visible()
        } else {
            binding.kycBanner.root.gone()
        }
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
        binding.kycBanner.btnCompleteKYC.setOnClickListener {
            startActivity(Intent(requireContext(), co.afrivest.ui.kyc.KycActivity::class.java))
        }
        refreshKYCBanner()
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
                // Refresh KYC banner with server value
                refreshKYCBanner()
                // Hide advisors browse button for advisor accounts
                updateAdvisorsFabVisibility(it.role)

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

                // Show/hide investment section in wallet card
                val llInvestmentSection = binding.walletCardHorizontal.root.findViewById<LinearLayout>(R.id.llInvestmentSection)
                if (it.investmentSummary != null && it.investmentSummary.hasInvestments()) {
                    llInvestmentSection.visible()
                } else {
                    llInvestmentSection.gone()
                }
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
            startActivity(Intent(requireContext(), co.afrivest.ui.qr.MyQrActivity::class.java))
        }
        binding.fabAdvisors.setOnClickListener {
            startActivity(Intent(requireContext(), co.afrivest.ui.advisors.AdvisorsActivity::class.java))
        }

        binding.btnNotification.setOnClickListener {
            // TODO: Navigate to notifications
            Toast.makeText(requireContext(), "Notifications", Toast.LENGTH_SHORT).show()
        }

    }

    private fun updateAdvisorsFabVisibility(role: String?) {
        if (role == "advisor") {
            binding.fabAdvisors.gone()
        } else {
            binding.fabAdvisors.visible()
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
            QuickAction(R.drawable.ic_loan, "Loans", "loans"),
            QuickAction(R.drawable.ic_insurance, "Insurance", "insurance"),
            QuickAction(R.drawable.ic_send, "Send Money", "send")
        )
        val adapter = QuickActionsAdapter(actions) { action ->
            when (action.key) {
                "invest" -> startActivity(Intent(requireContext(), InvestmentProductsActivity::class.java))
                "loans" -> startActivity(Intent(requireContext(), co.afrivest.ui.loans.LoansActivity::class.java))
                "insurance" -> startActivity(Intent(requireContext(), InsuranceListActivity::class.java))
                "send" -> startActivity(Intent(requireContext(), SendMoneyActivity::class.java))
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
                    val intent = Intent(requireContext(), co.afrivest.ui.investments.ProductDetailActivity::class.java).apply {
                        putExtra(co.afrivest.ui.investments.ProductDetailActivity.EXTRA_PRODUCT, product)
                    }
                    startActivity(intent)
                }
                binding.rvInvestments.apply {
                    layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    this.adapter = adapter
                }
            }
        }

        // Always show Hot Investments section
        binding.layoutHotInvestments.visible()
    }

    private val contactsPermissionLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                binding.btnFindFriends.gone()
                readAndLookupContacts()
            } else {
                binding.btnFindFriends.visible()
                binding.rvContacts.gone()
            }
        }

    private fun setupContacts() {
        // Render matched contacts when the lookup returns
        viewModel.matchedContacts.observe(viewLifecycleOwner) { contacts ->
            if (contacts.isEmpty()) {
                binding.rvContacts.gone()
                binding.btnFindFriends.visible()
                return@observe
            }
            binding.btnFindFriends.gone()
            binding.rvContacts.visible()
            val adapter = ContactsAdapter(contacts) { contact ->
                val intent = Intent(requireContext(), SendMoneyActivity::class.java).apply {
                    contact.userId?.let { putExtra("recipient_user_id", it) }
                    contact.uuid?.let { putExtra("recipient_uuid", it) }
                    putExtra("recipient_name", contact.name)
                }
                startActivity(intent)
            }
            binding.rvContacts.apply {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                this.adapter = adapter
            }
        }

        binding.btnFindFriends.setOnClickListener {
            contactsPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
        }

        // If permission already granted, load silently; otherwise show the button
        val granted = ContextCompat.checkSelfPermission(
            requireContext(), android.Manifest.permission.READ_CONTACTS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (granted) {
            binding.btnFindFriends.gone()
            readAndLookupContacts()
        } else {
            binding.btnFindFriends.visible()
            binding.rvContacts.gone()
        }
    }

    private fun readAndLookupContacts() {
        val phones = mutableListOf<String>()
        val emails = mutableListOf<String>()
        try {
            val resolver = requireContext().contentResolver
            resolver.query(
                android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER),
                null, null, null
            )?.use { c ->
                val idx = c.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (c.moveToNext()) {
                    if (idx >= 0) c.getString(idx)?.let { phones.add(it.replace(" ", "")) }
                }
            }
            resolver.query(
                android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                arrayOf(android.provider.ContactsContract.CommonDataKinds.Email.ADDRESS),
                null, null, null
            )?.use { c ->
                val idx = c.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Email.ADDRESS)
                while (c.moveToNext()) {
                    if (idx >= 0) c.getString(idx)?.let { emails.add(it.trim().lowercase()) }
                }
            }
        } catch (e: Exception) {
            Timber.e("Contact read failed: ${e.message}")
        }
        viewModel.lookupContacts(phones.distinct().take(500), emails.distinct().take(500))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}