package com.afrivest.app.data.repository

import com.afrivest.app.data.api.ApiResponse
import com.afrivest.app.data.api.ApiService
import com.afrivest.app.data.api.PaginatedResponse
import com.afrivest.app.data.model.Dashboard
import com.afrivest.app.data.model.Resource
import com.afrivest.app.data.model.Transaction
import com.afrivest.app.data.model.Wallet
import com.afrivest.app.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class WalletRepository @Inject constructor(
    private val apiService: ApiService
) {

    /**
     * Get dashboard data (user, wallets, recent transactions, statistics)
     */
    suspend fun getDashboard(): Resource<Dashboard> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDashboard()
            handleResponse(response)
        } catch (e: Exception) {
            Timber.e(e, "Get dashboard error")
            Resource.Error(e.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
        }
    }

    /**
     * Get all wallets
     */
    suspend fun getWallets(): Resource<List<Wallet>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getWallets()
            handleResponse(response)
        } catch (e: Exception) {
            Timber.e(e, "Get wallets error")
            Resource.Error(e.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
        }
    }

    /**
     * Get wallet by currency
     */
    suspend fun getWallet(currency: String): Resource<Wallet> = withContext(Dispatchers.IO) {
        try {
            val url = Constants.Endpoints.wallet(currency)
            val response = apiService.getWallet(url)
            handleResponse(response)
        } catch (e: Exception) {
            Timber.e(e, "Get wallet error")
            Resource.Error(e.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
        }
    }

    /**
     * Get wallet transactions with pagination
     */
    suspend fun getWalletTransactions(
        currency: String,
        page: Int = 1,
        perPage: Int = 15
    ): Resource<PaginatedResponse<Transaction>> = withContext(Dispatchers.IO) {
        try {
            val url = Constants.Endpoints.walletTransactions(currency)
            val response = apiService.getWalletTransactions(url, perPage, page)
            handleResponse(response)
        } catch (e: Exception) {
            Timber.e(e, "Get wallet transactions error")
            Resource.Error(e.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
        }
    }

    /**
     * Handle API response and convert to Resource
     */
    private fun <T> handleResponse(response: Response<ApiResponse<T>>): Resource<T> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body?.success == true) {
                Resource.Success(body.data)
            } else {
                Resource.Error(body?.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
            }
        } else {
            val errorMessage = response.message() ?: Constants.ErrorMessages.UNKNOWN_ERROR
            Resource.Error(errorMessage)
        }
    }
}