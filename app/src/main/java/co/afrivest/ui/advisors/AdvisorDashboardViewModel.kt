package co.afrivest.ui.advisors

import androidx.lifecycle.*
import co.afrivest.data.model.*
import co.afrivest.data.repository.AdvisorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdvisorDashboardViewModel @Inject constructor(
    private val repo: AdvisorRepository
) : ViewModel() {

    private val _dashboard = MutableLiveData<AdvisorDashboard?>(null)
    val dashboard: LiveData<AdvisorDashboard?> = _dashboard

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    fun load() = viewModelScope.launch {
        _loading.value = true
        when (val r = repo.dashboard()) {
            is Resource.Success -> _dashboard.value = r.data
            is Resource.Error -> _error.value = r.message
            is Resource.Loading -> {}
        }
        _loading.value = false
    }

    fun saveAvailability(slots: List<AvailabilitySlot>) = viewModelScope.launch {
        _loading.value = true
        when (val r = repo.setAvailability(slots)) {
            is Resource.Success -> { _message.value = "Availability updated"; load() }
            is Resource.Error -> _error.value = r.message
            is Resource.Loading -> {}
        }
        _loading.value = false
    }

    fun blockDate(date: String, reason: String?) = viewModelScope.launch {
        when (val r = repo.blockDate(date, reason)) {
            is Resource.Success -> { _message.value = "Date blocked"; load() }
            is Resource.Error -> _error.value = r.message
            is Resource.Loading -> {}
        }
    }

    fun unblockDate(id: Int) = viewModelScope.launch {
        when (val r = repo.unblockDate(id)) {
            is Resource.Success -> { _message.value = "Date block removed"; load() }
            is Resource.Error -> _error.value = r.message
            is Resource.Loading -> {}
        }
    }

    fun clearError() { _error.value = null }
    fun clearMessage() { _message.value = null }
}