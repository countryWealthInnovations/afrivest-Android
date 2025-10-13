package com.afrivest.app.data.repository

import com.afrivest.app.data.api.ApiService
import com.afrivest.app.data.api.DepositResponse
import com.afrivest.app.data.api.MobileMoneyDepositRequest
import com.afrivest.app.data.api.TransactionStatus
import com.afrivest.app.data.api.CardDepositRequest
import com.afrivest.app.data.local.SecurePreferences
import com.afrivest.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DepositRepository @Inject constructor(
    private val apiService: ApiService,
    private val securePreferences: SecurePreferences
) {

    suspend fun depositMobileMoney(
        amount: Double,
        currency: String,
        network: String,
        phoneNumber: String
    ): Resource<DepositResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = MobileMoneyDepositRequest(
                    amount = amount,
                    currency = currency,
                    payment_method = "mobile_money",
                    payment_provider = network.lowercase(),
                    phone_number = phoneNumber
                )

                val response = apiService.depositMobileMoney(request)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        Resource.Success(apiResponse.data) as Resource<DepositResponse>
                    } else {
                        Resource.Error(apiResponse.message ?: "Deposit failed")
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

    suspend fun depositCard(
        amount: Double,
        currency: String,
        cardNumber: String,
        cvv: String,
        expiryMonth: String,
        expiryYear: String
    ): Resource<DepositResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = CardDepositRequest(
                    amount = amount,
                    currency = currency,
                    card_number = cardNumber,
                    cvv = cvv,
                    expiry_month = expiryMonth,
                    expiry_year = expiryYear
                )

                val response = apiService.depositCard(request)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        Resource.Success(apiResponse.data) as Resource<DepositResponse>
                    } else {
                        Resource.Error(apiResponse.message ?: "Card deposit failed")
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

    suspend fun checkDepositStatus(transactionId: Int): Resource<TransactionStatus> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.checkDepositStatus(transactionId.toString())

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        Resource.Success(apiResponse.data) as Resource<TransactionStatus>
                    } else {
                        Resource.Error(apiResponse.message ?: "Failed to get status")
                    }
                } else {
                    Resource.Error("Failed to check status")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }
}