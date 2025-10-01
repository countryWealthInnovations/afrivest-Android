package com.afrivest.app.data.repository

import com.afrivest.app.data.api.*
import com.afrivest.app.data.local.SecurePreferences
import com.afrivest.app.data.model.ApiError
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.model.User
import com.afrivest.app.utils.Constants
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class TransferRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun p2pTransfer(
        recipientEmail: String,
        amount: Double,
        currency: String,
        description: String? = null
    ): Resource<TransferResponse> = withContext(Dispatchers.IO) {
        try {
            val request = P2PTransferRequest(recipientEmail, amount, currency, description)
            val response = apiService.p2pTransfer(request)
            handleResponse(response)
        } catch (e: Exception) {
            Timber.e(e, "P2P transfer error")
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