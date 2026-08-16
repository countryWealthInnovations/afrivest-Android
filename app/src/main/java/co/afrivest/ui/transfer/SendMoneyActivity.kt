package co.afrivest.ui.transfer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import co.afrivest.data.model.AppContact
import co.afrivest.data.model.Resource
import co.afrivest.databinding.ActivitySendMoneyBinding
import co.afrivest.ui.base.BaseActivity
import co.afrivest.ui.qr.ScanQrActivity
import co.afrivest.ui.transfer.adapter.ContactsAdapter
import co.afrivest.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SendMoneyActivity : BaseActivity() {
    private lateinit var binding: ActivitySendMoneyBinding
    private val viewModel: SendMoneyViewModel by viewModels()
    @Inject
    lateinit var preferencesManager: co.afrivest.data.local.PreferencesManager
    private lateinit var contactsAdapter: ContactsAdapter
    private var isContactSelected = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            //loadContacts()
        } else {
            Toast.makeText(this, "Contact permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val contactsPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) loadContacts()
    }

    private val scanLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.getStringExtra("recipient_uuid")?.let { viewModel.selectByUuid(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendMoneyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupListeners()

        // Request contacts permission
        if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS)
            == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            loadContacts()
        } else {
            contactsPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
        }

        // Prefill from a scanned QR (recipient_uuid) or a tapped home contact (recipient_user_id)
        intent.getStringExtra("recipient_uuid")?.let { uuid ->
            viewModel.selectByUuid(uuid)
        }
        intent.getIntExtra("recipient_user_id", -1).takeIf { it > 0 }?.let { uid ->
            val name = intent.getStringExtra("recipient_name") ?: "Recipient"
            viewModel.selectByUserId(uid, name)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Send Money"
    }

    private fun setupRecyclerView() {
        contactsAdapter = ContactsAdapter { contact ->
            if (contact.isRegistered) {
                viewModel.selectContact(contact)
            }
        }

        binding.rvContacts.apply {
            layoutManager = LinearLayoutManager(this@SendMoneyActivity)
            adapter = contactsAdapter
        }
    }

    private fun setupObservers() {
        // Loading
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.loadingOverlay.root.visible()
            } else {
                binding.loadingOverlay.root.gone()
            }
        }

        viewModel.filteredContacts.observe(this) { contacts ->
            contactsAdapter.submitList(contacts)
            val isSearching = binding.editTextSearch.text?.isNotEmpty() == true
            if (isContactSelected || !isSearching) {
                binding.rvContacts.gone()
                binding.tvEmptyState.gone()
                return@observe
            }
            if (contacts.isEmpty()) {
                binding.tvEmptyState.visible()
                binding.rvContacts.gone()
            } else {
                binding.tvEmptyState.gone()
                binding.rvContacts.visible()
            }
        }

        // Selected Contact
        viewModel.selectedContact.observe(this) { contact ->
            if (contact != null) {
                isContactSelected = true
                binding.layoutTransferForm.visible()
                binding.tvRecipientName.text = contact.name
                binding.tvRecipientIdentifier.text = contact.getDisplayIdentifier()
                binding.rvContacts.gone()
                binding.tvEmptyState.gone()
                binding.editTextSearch.setText(contact.name)
                binding.editTextSearch.clearFocus()
                val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(binding.editTextSearch.windowToken, 0)
            } else {
                isContactSelected = false
                binding.layoutTransferForm.gone()
            }
        }

        // Form Valid
        viewModel.isFormValid.observe(this) { isValid ->
            if (isValid) {
                binding.btnSendMoney.enable()
            } else {
                binding.btnSendMoney.disable()
            }
        }

        // Transfer Result
        viewModel.transferResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.loadingOverlay.root.visible()
                }
                is Resource.Success -> {
                    binding.loadingOverlay.root.gone()
                    showSuccessDialog(resource.data)
                }
                is Resource.Error -> {
                    binding.loadingOverlay.root.gone()
                    binding.textError.visible()
                    binding.textError.text = resource.message
                }
            }
        }

        viewModel.showManualEntry.observe(this) { show ->
            if (show) {
                binding.layoutManualEntry.visible()
            } else {
                binding.layoutManualEntry.gone()
            }
        }
    }

    private fun setupListeners() {
        // Search — show dropdown as user types
        binding.editTextSearch.doOnTextChanged { text, _, _, _ ->
            if (isContactSelected) return@doOnTextChanged
            val query = text?.toString() ?: ""
            viewModel.filterContacts(query)
            if (query.isNotEmpty()) {
                binding.rvContacts.visible()
            } else {
                binding.rvContacts.gone()
                binding.tvEmptyState.gone()
            }
        }

        // Scan QR
        binding.btnScanQr.setOnClickListener {
            scanLauncher.launch(Intent(this, ScanQrActivity::class.java))
        }

        // Manual Entry Toggle
        binding.btnManualEntry.visible()
        binding.btnManualEntry.setOnClickListener {
            if (binding.layoutManualEntry.visibility == android.view.View.VISIBLE) {
                binding.layoutManualEntry.gone()
                binding.btnManualEntry.text = "Enter phone/email manually"
            } else {
                binding.layoutManualEntry.visible()
                binding.btnManualEntry.text = "Hide manual entry"
            }
        }
        // binding.btnManualEntry.setOnClickListener {
        //     viewModel.toggleManualEntry()
        // }

        // Manual Search
        binding.btnSearchUser.setOnClickListener {
            val query = binding.editTextManualRecipient.text.toString()
            if (query.isNotEmpty()) {
                viewModel.searchManualRecipient(query)
            }
        }

        // Amount
        val currency = preferencesManager.defaultCurrency ?: "UGX"
        val minAmount = when (currency) {
            "UGX" -> "5,000"
            "KES" -> "50"
            "NGN" -> "500"
            else -> "1"
        }
        binding.tvAmountLabel.text = "Amount ($currency)"
        binding.editTextAmount.hint = "Enter amount (min $minAmount)"

        binding.editTextAmount.doOnTextChanged { text, _, _, _ ->
            viewModel.setAmount(text?.toString() ?: "")
            updateSummary()
        }

        // Description
        binding.editTextDescription.doOnTextChanged { text, _, _, _ ->
            viewModel.setDescription(text?.toString() ?: "")
        }

        // Send Button
        binding.btnSendMoney.setOnClickListener {
            viewModel.initiateTransfer()
        }
    }

    private fun updateSummary() {
        val amount = binding.editTextAmount.text.toString().toDoubleOrNull() ?: 0.0
        val currency = preferencesManager.defaultCurrency ?: "UGX"
        binding.tvSummaryAmount.text = String.format("%,.2f %s", amount, currency)
        binding.tvSummaryFee.text = "0.00 $currency"
        binding.tvSummaryTotal.text = String.format("%,.2f %s", amount, currency)
    }

    private fun checkContactPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                loadContacts()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    private fun loadContacts() {
        val contacts = mutableListOf<AppContact>()

        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIndex)
                val number = cursor.getString(numberIndex)

                contacts.add(AppContact(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name ?: "Unknown",
                    phoneNumber = number,
                    email = null,
                    userId = null,
                    isRegistered = false
                ))
            }
        }

        // Also get emails
        contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null, null, null, null
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME)
            val emailIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)

            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIndex)
                val email = cursor.getString(emailIndex)

                contacts.add(AppContact(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name ?: "Unknown",
                    phoneNumber = null,
                    email = email,
                    userId = null,
                    isRegistered = false
                ))
            }
        }

        viewModel.setContacts(contacts)
    }

    private fun showSuccessDialog(response: co.afrivest.data.model.P2PTransferResponse?) {
        response?.let {
            MaterialAlertDialogBuilder(this)
                .setTitle("Transfer Successful")
                .setMessage("Sent ${it.transaction.amount} ${it.transaction.currency} to ${it.transaction.recipient}")
                .setPositiveButton("Done") { _, _ ->
                    finish()
                }
                .setCancelable(false)
                .show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}