package com.afrivest.app.ui.transfer

import android.Manifest
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
import com.afrivest.app.data.model.AppContact
import com.afrivest.app.data.model.Resource
import com.afrivest.app.databinding.ActivitySendMoneyBinding
import com.afrivest.app.ui.transfer.adapter.ContactsAdapter
import com.afrivest.app.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SendMoneyActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySendMoneyBinding
    private val viewModel: SendMoneyViewModel by viewModels()
    private lateinit var contactsAdapter: ContactsAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            //loadContacts()
        } else {
            Toast.makeText(this, "Contact permission denied", Toast.LENGTH_SHORT).show()
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
        //checkContactPermission()
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

        // Contacts
        viewModel.filteredContacts.observe(this) { contacts ->
            contactsAdapter.submitList(contacts)

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
                binding.layoutTransferForm.visible()
                binding.tvRecipientName.text = contact.name
                binding.tvRecipientIdentifier.text = contact.getDisplayIdentifier()
            } else {
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

        // Manual search - Always show manual entry by default
        viewModel.showManualEntry.observe(this) { show ->
            // Always show manual entry, hide contacts section
            binding.layoutManualEntry.visible()
            binding.rvContacts.gone()
            binding.tvEmptyState.gone()
        }
    }

    private fun setupListeners() {
        // Search
//        binding.editTextSearch.doOnTextChanged { text, _, _, _ ->
//            viewModel.filterContacts(text?.toString() ?: "")
//        }

        // Manual Entry Toggle - Hidden since manual entry is default
        binding.btnManualEntry.gone()
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
        binding.tvSummaryAmount.text = String.format("%,.2f UGX", amount)
        binding.tvSummaryFee.text = "0.00 UGX"
        binding.tvSummaryTotal.text = String.format("%,.2f UGX", amount)
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

    private fun showSuccessDialog(response: com.afrivest.app.data.model.P2PTransferResponse?) {
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