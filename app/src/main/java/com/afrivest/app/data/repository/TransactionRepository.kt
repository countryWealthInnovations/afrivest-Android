package com.afrivest.app.data.repository

import com.afrivest.app.data.api.*
import com.afrivest.app.data.local.SecurePreferences
import com.afrivest.app.data.model.ApiError
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.model.Transaction
import com.afrivest.app.data.model.User
import com.afrivest.app.utils.Constants
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
    ): Resource<PaginatedResponse<Transaction>> = withContext(Dispatchers.IO) {
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