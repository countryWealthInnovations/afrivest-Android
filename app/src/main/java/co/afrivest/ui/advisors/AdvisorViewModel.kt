package co.afrivest.ui.advisors

import androidx.lifecycle.*
import co.afrivest.data.model.*
import co.afrivest.data.repository.AdvisorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdvisorViewModel @Inject constructor(
    private val repo: AdvisorRepository
) : ViewModel() {

    private val _advisors = MutableLiveData<List<Advisor>>(emptyList())
    val advisors: LiveData<List<Advisor>> = _advisors

    private val _detail = MutableLiveData<Advisor?>(null)
    val detail: LiveData<Advisor?> = _detail

    private val _bookings = MutableLiveData<List<AdvisorBookingDto>>(emptyList())
    val bookings: LiveData<List<AdvisorBookingDto>> = _bookings

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _booked = MutableLiveData<AdvisorBookingDto?>(null)
    val booked: LiveData<AdvisorBookingDto?> = _booked

    fun loadAdvisors() = viewModelScope.launch {
        _loading.value = true
        when (val r = repo.list()) {
            is Resource.Success -> _advisors.value = r.data ?: emptyList()
            is Resource.Error -> _error.value = r.message
            is Resource.Loading -> {}
        }
        _loading.value = false
    }

    fun loadDetail(id: Int) = viewModelScope.launch {
        _loading.value = true
        when (val r = repo.detail(id)) {
            is Resource.Success -> _detail.value = r.data
            is Resource.Error -> _error.value = r.message
            is Resource.Loading -> {}
        }
        _loading.value = false
    }

    fun loadMyBookings() = viewModelScope.launch {
        _loading.value = true
        when (val r = repo.myBookings()) {
            is Resource.Success -> _bookings.value = r.data ?: emptyList()
            is Resource.Error -> _error.value = r.message
            is Resource.Loading -> {}
        }
        _loading.value = false
    }

    fun book(id: Int, datetime: String, notes: String?) = viewModelScope.launch {
        _loading.value = true
        when (val r = repo.book(id, datetime, notes)) {
            is Resource.Success -> _booked.value = r.data
            is Resource.Error -> _error.value = r.message
            is Resource.Loading -> {}
        }
        _loading.value = false
    }

    fun clearError() { _error.value = null }
    fun clearBooked() { _booked.value = null }
}