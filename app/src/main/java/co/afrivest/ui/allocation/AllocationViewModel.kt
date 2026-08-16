package co.afrivest.ui.allocation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.afrivest.data.model.Resource
import co.afrivest.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllocationViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val current = LinkedHashMap<String, Int>()

    private val _targets = MutableLiveData<List<String>>(emptyList())
    val targets: LiveData<List<String>> = _targets

    private val _allocation = MutableLiveData<Map<String, Int>>(emptyMap())
    val allocation: LiveData<Map<String, Int>> = _allocation

    private val _total = MutableLiveData(0)
    val total: LiveData<Int> = _total

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _saved = MutableLiveData(false)
    val saved: LiveData<Boolean> = _saved

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            when (val r = profileRepository.getAllocation()) {
                is Resource.Success -> {
                    val data = r.data!!
                    current.clear()
                    // Every allowed target present, defaulting to the server value or 0
                    data.allowed_targets.forEach { t ->
                        current[t] = data.allocation[t] ?: 0
                    }
                    _targets.value = data.allowed_targets
                    publish()
                }
                is Resource.Error -> _error.value = r.message
                is Resource.Loading -> {}
            }
            _loading.value = false
        }
    }

    fun adjust(target: String, delta: Int) {
        val next = ((current[target] ?: 0) + delta).coerceIn(0, 100)
        current[target] = next
        publish()
    }

    fun save() {
        if ((_total.value ?: 0) != 100) {
            _error.value = "Allocation must total 100%"
            return
        }
        viewModelScope.launch {
            _loading.value = true
            when (val r = profileRepository.updateAllocation(HashMap(current))) {
                is Resource.Success -> _saved.value = true
                is Resource.Error -> _error.value = r.message
                is Resource.Loading -> {}
            }
            _loading.value = false
        }
    }

    fun clearError() { _error.value = null }

    private fun publish() {
        _allocation.value = LinkedHashMap(current)
        _total.value = current.values.sum()
    }
}