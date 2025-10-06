package com.afrivest.app.data.repository

import com.afrivest.app.data.api.ApiService
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DepositRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun initiateSDKDeposit(amount: Double, currency: String): Resource<SDKInitiateResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = mapOf(
                    "amount" to amount,
                    "currency" to currency
                )

                val response = apiService.initiateSDKDeposit(request)

                if (response.success && response.data != null) {
                    Resource.Success(response.data)
                } else {
                    Resource.Error(response.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    suspend fun verifySDKDeposit(
        transactionId: Int,
        flwRef: String,
        status: String
    ): Resource<VerifyDepositResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = mapOf(
                    "transaction_id" to transactionId,
                    "flw_ref" to flwRef,
                    "status" to status
                )

                val response = apiService.verifySDKDeposit(request)

                if (response.success && response.data != null) {
                    Resource.Success(response.data)
                } else {
                    Resource.Error(response.message ?: "Verification failed")
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Network error")
            }
        }
    }
}

// Models
data class SDKInitiateResponse(
    val transaction_id: Int,
    val tx_ref: String,
    val amount: Double,
    val currency: String,
    val user: UserInfo,
    val sdk_config: SDKConfig
)

data class UserInfo(
    val email: String,
    val name: String,
    val phone_number: String
)

data class SDKConfig(
    val public_key: String,
    val encryption_key: String
)

data class VerifyDepositResponse(
    val transaction: Transaction,
    val wallet: WalletInfo?
)

data class WalletInfo(
    val currency: String,
    val balance: String
)