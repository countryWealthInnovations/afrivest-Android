package com.afrivest.app.ui.transfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afrivest.app.data.model.AppContact
import com.afrivest.app.data.model.P2PTransferResponse
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.repository.TransferRepository
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

    private fun checkRegisteredUsers(contacts: List<AppContact>) {
        viewModelScope.launch {
            val updatedContacts = mutableListOf<AppContact>()

            for (contact in contacts) {
                val query = contact.phoneNumber ?: contact.email ?: continue

                val result = transferRepository.searchUser(query)

                if (result is Resource.Success && result.data?.found == true) {
                    val user = result.data.user
                    updatedContacts.add(contact.copy(
                        name = user?.name ?: contact.name,
                        userId = user?.id,
                        isRegistered = true
                    ))
                } else {
                    updatedContacts.add(contact)
                }
            }

            _contacts.value = updatedContacts
            _filteredContacts.value = updatedContacts.filter { it.isRegistered }
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
        _isFormValid.value = _selectedContact.value != null && amountValue >= 10000
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