package co.afrivest.data.repository

import co.afrivest.data.api.ApiService
import co.afrivest.data.model.BankWithdrawRequest
import co.afrivest.data.model.Resource
import co.afrivest.data.model.WithdrawRequest
import co.afrivest.data.model.WithdrawResponse
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
        walletCurrency: String,
        network: String,
        phoneNumber: String
    ): Resource<WithdrawResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = WithdrawRequest(
                    amount = amount,
                    currency = currency,
                    wallet_currency = walletCurrency,
                    network = network,
                    phone_number = phoneNumber
                )
                val response = apiService.withdrawMobileMoney(request)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) Resource.Success(apiResponse.data)
                    else Resource.Error(apiResponse.message ?: "Withdrawal failed")
                } else {
                    Resource.Error(response.errorBody()?.string() ?: "Unknown error")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun withdrawBankTransfer(
        amount: Double,
        currency: String,
        walletCurrency: String,
        bankCode: String,
        accountNumber: String,
        accountName: String
    ): Resource<WithdrawResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = BankWithdrawRequest(
                    amount = amount,
                    currency = currency,
                    wallet_currency = walletCurrency,
                    bank_code = bankCode,
                    account_number = accountNumber,
                    account_name = accountName
                )
                val response = apiService.withdrawBankTransfer(request)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) Resource.Success(apiResponse.data)
                    else Resource.Error(apiResponse.message ?: "Withdrawal failed")
                } else {
                    Resource.Error(response.errorBody()?.string() ?: "Unknown error")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }
}