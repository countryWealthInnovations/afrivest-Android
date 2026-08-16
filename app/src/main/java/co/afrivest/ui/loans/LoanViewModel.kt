package co.afrivest.ui.loans

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.afrivest.data.model.Loan
import co.afrivest.data.model.LoanTerm
import co.afrivest.data.model.Resource
import co.afrivest.data.repository.LoanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoanViewModel @Inject constructor(
    private val repository: LoanRepository
) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    private val _terms = MutableLiveData<List<LoanTerm>>(emptyList())
    val terms: LiveData<List<LoanTerm>> = _terms

    private val _borrowed = MutableLiveData<List<Loan>>(emptyList())
    val borrowed: LiveData<List<Loan>> = _borrowed

    private val _lent = MutableLiveData<List<Loan>>(emptyList())
    val lent: LiveData<List<Loan>> = _lent

    private val _available = MutableLiveData<List<Loan>>(emptyList())
    val available: LiveData<List<Loan>> = _available

    private val _detail = MutableLiveData<Loan?>(null)
    val detail: LiveData<Loan?> = _detail

    // One-shot success signal for request/fund/repay
    private val _actionOk = MutableLiveData<Loan?>(null)
    val actionOk: LiveData<Loan?> = _actionOk

    fun clearError() { _error.value = null }
    fun clearMessage() { _message.value = null }
    fun clearAction() { _actionOk.value = null }

    fun loadTerms() = viewModelScope.launch {
        when (val r = repository.getTerms()) {
            is Resource.Success -> _terms.value = r.data ?: emptyList()
            is Resource.Error -> _error.value = r.message
            is Resource.Loading -> {}
        }
    }

    fun loadMyLoans() = viewModelScope.launch {
        _loading.value = true
        when (val r = repository.getMyLoans()) {
            is Resource.Success -> {
                _borrowed.value = r.data?.borrowed ?: emptyList()
                _lent.value = r.data?.lent ?: emptyList()
            }
            is Resource.Error -> _error.value = r.message
            is Resource.Loading -> {}
        }
        _loading.value = false
    }

    fun loadAvailable() = viewModelScope.launch {
        _loading.value = true
        when (val r = repository.getAvailable()) {
            is Resource.Success -> _available.value = r.data ?: emptyList()
            is Resource.Error -> _error.value = r.message
            is Resource.Loading -> {}
        }
        _loading.value = false
    }

    fun loadDetail(uuid: String) = viewModelScope.launch {
        _loading.value = true
        when (val r = repository.getLoan(uuid)) {
            is Resource.Success -> _detail.value = r.data
            is Resource.Error -> _error.value = r.message
            is Resource.Loading -> {}
        }
        _loading.value = false
    }

    fun requestLoan(amount: Double, term: String, currency: String, purpose: String?) =
        viewModelScope.launch {
            _loading.value = true
            when (val r = repository.requestLoan(amount, term, currency, purpose)) {
                is Resource.Success -> { _actionOk.value = r.data; _message.value = "Loan request submitted" }
                is Resource.Error -> _error.value = r.message
                is Resource.Loading -> {}
            }
            _loading.value = false
        }

    fun fund(uuid: String) = viewModelScope.launch {
        _loading.value = true
        when (val r = repository.fund(uuid)) {
            is Resource.Success -> { _actionOk.value = r.data; _message.value = "Loan funded" }
            is Resource.Error -> _error.value = r.message
            is Resource.Loading -> {}
        }
        _loading.value = false
    }

    fun repay(uuid: String, amount: Double) = viewModelScope.launch {
        _loading.value = true
        when (val r = repository.repay(uuid, amount)) {
            is Resource.Success -> { _actionOk.value = r.data; _message.value = "Repayment successful"; _detail.value = r.data }
            is Resource.Error -> _error.value = r.message
            is Resource.Loading -> {}
        }
        _loading.value = false
    }
}