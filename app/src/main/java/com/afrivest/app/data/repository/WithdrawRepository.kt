package com.afrivest.app.data.repository

import com.afrivest.app.data.api.ApiService
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.model.WithdrawRequest
import com.afrivest.app.data.model.WithdrawResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WithdrawRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun withdrawMobileMoney(
        amount: Double,
        currency: String,
        network: String,
        phoneNumber: String
    ): Resource<WithdrawResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = WithdrawRequest(
                    amount = amount,
                    currency = currency,
                    network = network,
                    phone_number = phoneNumber
                )

                val response = apiService.withdrawMobileMoney(request)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        Resource.Success(apiResponse.data)
                    } else {
                        Resource.Error(apiResponse.message ?: "Withdrawal failed")
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
}