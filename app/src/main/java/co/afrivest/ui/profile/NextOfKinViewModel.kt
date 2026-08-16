package co.afrivest.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.afrivest.data.api.ApiService
import co.afrivest.data.api.NextOfKinData
import co.afrivest.data.api.NextOfKinRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NextOfKinViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    val relationships = listOf(
        "spouse" to "Spouse",
        "parent" to "Parent",
        "sibling" to "Sibling",
        "child" to "Child",
        "other" to "Other"
    )

    private val _kin = MutableLiveData<NextOfKinData?>()
    val kin: LiveData<NextOfKinData?> = _kin

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _saveSuccess = MutableLiveData(false)
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getNextOfKin()
                if (response.isSuccessful) {
                    _kin.value = response.body()?.data
                } else {
                    _errorMessage.value = "Could not load next of kin"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun save(name: String, relationship: String, phone: String, email: String) {
        if (name.isBlank() || relationship.isBlank() || phone.isBlank() || email.isBlank()) {
            _errorMessage.value = "All fields are required"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.saveNextOfKin(
                    NextOfKinRequest(name, relationship, phone, email)
                )
                if (response.isSuccessful) {
                    _kin.value = response.body()?.data
                    _saveSuccess.value = true
                } else {
                    _errorMessage.value = "Could not save next of kin"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to save"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.deleteNextOfKin()
                if (response.isSuccessful) {
                    _kin.value = null
                    _saveSuccess.value = true
                } else {
                    _errorMessage.value = "Could not remove next of kin"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to remove"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() { _errorMessage.value = null }
}