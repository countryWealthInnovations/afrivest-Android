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
    private val transferRepository: TransferRepository
) : ViewModel() {

    private val _contacts = MutableLiveData<List<AppContact>>()
    val contacts: LiveData<List<AppContact>> = _contacts

    private val _filteredContacts = MutableLiveData<List<AppContact>>()
    val filteredContacts: LiveData<List<AppContact>> = _filteredContacts

    private val _selectedContact = MutableLiveData<AppContact?>()
    val selectedContact: LiveData<AppContact?> = _selectedContact

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
            try {
                val phones = contacts.mapNotNull { it.phoneNumber }
                val emails = contacts.mapNotNull { it.email }
                val matched = transferRepository.lookupContacts(phones, emails)

                // Build phone → matched map
                val phoneMap = matched.associateBy { it.phoneNumber }

                val updated = contacts.map { contact ->
                    val match = contact.phoneNumber?.let { phoneMap[it] }
                    if (match != null) {
                        contact.copy(
                            name         = match.name,
                            userId       = match.userId,
                            isRegistered = true
                        )
                    } else contact
                }

                _contacts.value = updated
                _filteredContacts.value = updated.filter { it.isRegistered }
            } catch (e: Exception) {
                android.util.Log.w("SendMoneyVM", "Bulk contact lookup failed: ${e.message}")
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
        _isFormValid.value = _selectedContact.value != null && amountValue >= 5000
    }

    fun initiateTransfer() {
        val contact = _selectedContact.value ?: return
        val userId = contact.userId ?: return
        val amountValue = _amount.value?.toDoubleOrNull() ?: return

        viewModelScope.launch {
            _transferResult.value = Resource.Loading()

            val result = transferRepository.transferP2P(
                recipientId = userId,
                amount = amountValue,
                currency = "UGX",
                description = _description.value?.ifEmpty { null }
            )

            _transferResult.value = result
        }
    }
}