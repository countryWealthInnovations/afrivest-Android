package co.afrivest.ui.transfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.afrivest.data.model.AppContact
import co.afrivest.data.model.P2PTransferResponse
import co.afrivest.data.model.Resource
import co.afrivest.data.repository.TransferRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SendMoneyViewModel @Inject constructor(
    private val transferRepository: TransferRepository,
    private val preferencesManager: co.afrivest.data.local.PreferencesManager
) : ViewModel() {

    private val _contacts = MutableLiveData<List<AppContact>>()
    val contacts: LiveData<List<AppContact>> = _contacts

    private val _filteredContacts = MutableLiveData<List<AppContact>>()
    val filteredContacts: LiveData<List<AppContact>> = _filteredContacts

    private val _selectedContact = MutableLiveData<AppContact?>()
    val selectedContact: LiveData<AppContact?> = _selectedContact

    private val _recipientUuid = MutableLiveData<String?>(null)

    private val _amount = MutableLiveData<String>("")
    val amount: LiveData<String> = _amount

    private val _description = MutableLiveData<String>("")
    val description: LiveData<String> = _description

    private val _showManualEntry = MutableLiveData(false)
    val showManualEntry: LiveData<Boolean> = _showManualEntry

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isFormValid = MutableLiveData(false)
    val isFormValid: LiveData<Boolean> = _isFormValid

    private val _transferResult = MutableLiveData<Resource<P2PTransferResponse>>()
    val transferResult: LiveData<Resource<P2PTransferResponse>> = _transferResult

    fun setContacts(contacts: List<AppContact>) {
        _contacts.value = contacts
        checkRegisteredUsers(contacts)
    }

    fun loadContacts() {
        // Triggers the Activity to load contacts via ContactsHelper and call setContacts()
        // Actual device reading happens in Activity to avoid needing Application context in VM
        _showManualEntry.value = false
    }

    private fun checkRegisteredUsers(contacts: List<AppContact>) {
        viewModelScope.launch {
            val phones = contacts.mapNotNull { it.phoneNumber }
            val emails = contacts.mapNotNull { it.email }

            if (phones.isEmpty() && emails.isEmpty()) return@launch

            when (val result = transferRepository.lookupContacts(phones, emails)) {
                is Resource.Success -> {
                    val matches = result.data?.contacts ?: emptyList()
                    val phoneMap = matches.associateBy { it.phone }

                    val updated = contacts.map { contact ->
                        val match = contact.phoneNumber?.let { phoneMap[it] }
                        if (match != null) {
                            contact.copy(
                                name = match.name,
                                userId = match.user_id,
                                isRegistered = true
                            )
                        } else contact
                    }

                    _contacts.value = updated
                    _filteredContacts.value = updated.filter { it.isRegistered }
                }
                is Resource.Error -> {
                    android.util.Log.w("SendMoneyVM", "Bulk contact lookup failed: ${result.message}")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun filterContacts(query: String) {
        val allContacts = _contacts.value ?: emptyList()

        _filteredContacts.value = if (query.isEmpty()) {
            allContacts.filter { it.isRegistered }
        } else {
            allContacts.filter { contact ->
                contact.isRegistered && (
                        contact.name.contains(query, ignoreCase = true) ||
                                contact.phoneNumber?.contains(query) == true ||
                                contact.email?.contains(query, ignoreCase = true) == true
                        )
            }
        }
    }

    fun selectContact(contact: AppContact) {
        _selectedContact.value = contact
        _recipientUuid.value = null
        validateForm()
    }

    fun selectByUserId(userId: Int, name: String) {
        _selectedContact.value = AppContact(
            id = java.util.UUID.randomUUID().toString(),
            name = name, phoneNumber = null, email = null,
            userId = userId, isRegistered = true
        )
        _recipientUuid.value = null
        validateForm()
    }

    fun selectByUuid(uuid: String) {
        _recipientUuid.value = uuid
        _selectedContact.value = AppContact(
            id = java.util.UUID.randomUUID().toString(),
            name = "Loading...", phoneNumber = null, email = null,
            userId = null, isRegistered = true
        )
        viewModelScope.launch {
            when (val result = transferRepository.lookupByUuid(uuid)) {
                is Resource.Success -> {
                    val user = result.data?.user
                    if (result.data?.found == true && user != null) {
                        _selectedContact.value = AppContact(
                            id = java.util.UUID.randomUUID().toString(),
                            name = user.name,
                            phoneNumber = user.phone_number,
                            email = user.email,
                            userId = user.id,
                            isRegistered = true
                        )
                    }
                }
                else -> {}
            }
        }
        validateForm()
    }

    fun setAmount(amount: String) {
        _amount.value = amount
        validateForm()
    }

    fun setDescription(description: String) {
        _description.value = description
    }

    fun toggleManualEntry() {
        _showManualEntry.value = !(_showManualEntry.value ?: false)
    }

    fun searchManualRecipient(query: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = transferRepository.searchUser(query)

            if (result is Resource.Success && result.data?.found == true) {
                val user = result.data.user
                if (user != null) {
                    val contact = AppContact(
                        id = java.util.UUID.randomUUID().toString(),
                        name = user.name,
                        phoneNumber = user.phone_number,
                        email = user.email,
                        userId = user.id,
                        isRegistered = true
                    )
                    _selectedContact.value = contact
                    _showManualEntry.value = false
                }
            }

            _isLoading.value = false
        }
    }

    private fun validateForm() {
        val amountValue = _amount.value?.toDoubleOrNull() ?: 0.0
        val minAmount = when (preferencesManager.defaultCurrency) {
            "UGX" -> 5000.0
            "KES" -> 50.0
            "NGN" -> 500.0
            else -> 1.0
        }
        val hasRecipient = (_selectedContact.value?.userId != null) || (_recipientUuid.value != null)
        _isFormValid.value = hasRecipient && amountValue >= minAmount
    }

    fun initiateTransfer() {
        val amountValue = _amount.value?.toDoubleOrNull() ?: return
        val uuid = _recipientUuid.value
        val userId = _selectedContact.value?.userId
        if (uuid == null && userId == null) return

        viewModelScope.launch {
            _transferResult.value = Resource.Loading()

            val result = transferRepository.transferP2P(
                recipientId = userId,
                recipientUuid = uuid,
                amount = amountValue,
                currency = preferencesManager.defaultCurrency ?: "UGX",
                description = _description.value?.ifEmpty { null }
            )

            _transferResult.value = result
        }
    }
}