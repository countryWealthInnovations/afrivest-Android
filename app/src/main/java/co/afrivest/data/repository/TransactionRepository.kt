package co.afrivest.data.repository

import co.afrivest.data.api.*
import co.afrivest.data.local.SecurePreferences
import co.afrivest.data.model.ApiError
import co.afrivest.data.model.Resource
import co.afrivest.data.model.Transaction
import co.afrivest.data.model.User
import co.afrivest.utils.Constants
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class TransactionRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getTransactions(
        page: Int = 1,
        perPage: Int = 15,
        type: String? = null,
        status: String? = null,
        currency: String? = null
    ): Resource<List<Transaction>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTransactions(perPage, page, type, status, currency)
            handleResponse(response)
        } catch (e: Exception) {
            Timber.e(e, "Get transactions error")
            Resource.Error(e.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
        }
    }

    suspend fun getTransaction(id: Int): Resource<Transaction> = withContext(Dispatchers.IO) {
        try {
            val url = Constants.Endpoints.transaction(id)
            val response = apiService.getTransaction(url)
            handleResponse(response)
        } catch (e: Exception) {
            Timber.e(e, "Get transaction error")
            Resource.Error(e.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
        }
    }

    private fun <T> handleResponse(response: Response<ApiResponse<T>>): Resource<T> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body?.success == true) {
                Resource.Success(body.data)
            } else {
                Resource.Error(body?.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
            }
        } else {
            Resource.Error(response.message() ?: Constants.ErrorMessages.UNKNOWN_ERROR)
        }
    }
}