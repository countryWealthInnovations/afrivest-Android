package co.afrivest.data.repository

import co.afrivest.data.api.ApiService
import co.afrivest.data.model.*
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

    suspend fun lookupContacts(phones: List<String>, emails: List<String>): List<AppContact> {
        return try {
            val body = mapOf("phones" to phones, "emails" to emails)
            val response = apiService.lookupContacts(body)
            if (response.isSuccessful) {
                response.body()?.data?.contacts?.map {
                    AppContact(
                        id           = it.user_id.toString(),
                        name         = it.name,
                        phoneNumber  = it.phone,
                        email        = null,
                        userId       = it.user_id,
                        isRegistered = true
                    )
                } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
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