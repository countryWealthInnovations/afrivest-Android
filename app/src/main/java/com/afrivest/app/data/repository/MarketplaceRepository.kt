package com.afrivest.app.data.repository

import com.afrivest.app.data.api.*
import com.afrivest.app.data.local.SecurePreferences
import com.afrivest.app.data.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketplaceRepository @Inject constructor(
    private val apiService: ApiService,
    private val securePreferences: SecurePreferences
) {

    suspend fun getGoldProducts(): Resource<List<InvestmentProduct>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getGoldProducts()

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        Resource.Success(apiResponse.data)
                    } else {
                        Resource.Error(apiResponse.message ?: "Failed to fetch gold products")
                    }
                } else {
                    Resource.Error("Failed to fetch gold products")
                }
            } catch (e: Exception) {
                Timber.e(e, "Get gold products error")
                Resource.Error(e.message ?: "Network error occurred")
            }
        }
    }

    suspend fun getCurrentGoldPrice(): Resource<GoldPrice> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCurrentGoldPrice()

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        Resource.Success(apiResponse.data)
                    } else {
                        Resource.Error(apiResponse.message ?: "Failed to fetch gold price")
                    }
                } else {
                    Resource.Error("Failed to fetch gold price")
                }
            } catch (e: Exception) {
                Timber.e(e, "Get gold price error")
                Resource.Error(e.message ?: "Network error occurred")
            }
        }
    }
}