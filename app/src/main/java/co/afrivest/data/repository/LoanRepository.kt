package co.afrivest.data.repository

import co.afrivest.data.api.ApiService
import co.afrivest.data.model.Loan
import co.afrivest.data.model.LoanRepayBody
import co.afrivest.data.model.LoanRequestBody
import co.afrivest.data.model.LoanTerm
import co.afrivest.data.model.MyLoans
import co.afrivest.data.model.Resource
import co.afrivest.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoanRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getTerms(): Resource<List<LoanTerm>> = safeCall {
        val r = apiService.getLoanTerms()
        unwrap(r.isSuccessful, r.body()?.success, r.body()?.data, r.body()?.message)
    }

    suspend fun getAvailable(): Resource<List<Loan>> = safeCall {
        val r = apiService.getAvailableLoans()
        unwrap(r.isSuccessful, r.body()?.success, r.body()?.data, r.body()?.message)
    }

    suspend fun getMyLoans(): Resource<MyLoans> = safeCall {
        val r = apiService.getMyLoans()
        unwrap(r.isSuccessful, r.body()?.success, r.body()?.data, r.body()?.message)
    }

    suspend fun requestLoan(
        amount: Double, term: String, currency: String, purpose: String?
    ): Resource<Loan> = safeCall {
        val r = apiService.requestLoan(LoanRequestBody(amount, term, currency, purpose))
        unwrap(r.isSuccessful, r.body()?.success, r.body()?.data?.loan, r.body()?.message)
    }

    suspend fun getLoan(uuid: String): Resource<Loan> = safeCall {
        val r = apiService.getLoan(uuid)
        unwrap(r.isSuccessful, r.body()?.success, r.body()?.data, r.body()?.message)
    }

    suspend fun fund(uuid: String): Resource<Loan> = safeCall {
        val r = apiService.fundLoan(uuid)
        unwrap(r.isSuccessful, r.body()?.success, r.body()?.data?.loan, r.body()?.message)
    }

    suspend fun repay(uuid: String, amount: Double): Resource<Loan> = safeCall {
        val r = apiService.repayLoan(uuid, LoanRepayBody(amount))
        unwrap(r.isSuccessful, r.body()?.success, r.body()?.data?.loan, r.body()?.message)
    }

    private fun <T> unwrap(httpOk: Boolean, success: Boolean?, data: T?, message: String?): Resource<T> {
        return if (httpOk && success == true && data != null) {
            Resource.Success(data)
        } else {
            Resource.Error(message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
        }
    }

    private suspend fun <T> safeCall(block: suspend () -> Resource<T>): Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                block()
            } catch (e: Exception) {
                Timber.e(e, "Loan API call failed")
                Resource.Error(e.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
            }
        }
    }
}