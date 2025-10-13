package com.afrivest.app.data.repository

import com.afrivest.app.data.api.ApiService
import com.afrivest.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun transferP2P(
        recipientId: Int,
        amount: Double,
        currency: String,
        description: String?
    ): Resource<P2PTransferResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = P2PTransferRequest(
                    recipient_id = recipientId,
                    amount = amount,
                    currency = currency,
                    description = description
                )

                val response = apiService.transferP2P(request)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        Resource.Success(apiResponse.data)
                    } else {
                        Resource.Error(apiResponse.message ?: "Transfer failed")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Resource.Error(errorBody ?: "Unknown error occurred")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error occurred")
            }
        }
    }

    suspend fun searchUser(query: String): Resource<UserSearchResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchUser(query)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        Resource.Success(apiResponse.data)
                    } else {
                        Resource.Error(apiResponse.message ?: "User not found")
                    }
                } else {
                    Resource.Error("User not found")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error occurred")
            }
        }
    }
}