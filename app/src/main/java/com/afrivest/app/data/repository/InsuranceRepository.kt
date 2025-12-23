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
class InsuranceRepository @Inject constructor(
    private val apiService: ApiService,
    private val securePreferences: SecurePreferences
) {

    suspend fun getInsuranceProviders(): Resource<List<InsuranceProvider>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getInsuranceProviders()

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        Resource.Success(apiResponse.data)
                    } else {
                        Resource.Error(apiResponse.message ?: "Failed to fetch providers")
                    }
                } else {
                    Resource.Error("Failed to fetch providers")
                }
            } catch (e: Exception) {
                Timber.e(e, "Get insurance providers error")
                Resource.Error(e.message ?: "Network error occurred")
            }
        }
    }

    suspend fun purchasePolicy(request: PurchaseInsurancePolicyRequest): Resource<InsurancePolicy> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.purchaseInsurancePolicy(request)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        Resource.Success(apiResponse.data)
                    } else {
                        Resource.Error(apiResponse.message ?: "Purchase failed")
                    }
                } else {
                    Resource.Error("Purchase failed")
                }
            } catch (e: Exception) {
                Timber.e(e, "Purchase policy error")
                Resource.Error(e.message ?: "Network error occurred")
            }
        }
    }

    suspend fun getInsurancePolicies(
        status: String? = null,
        policyType: String? = null
    ): Resource<List<InsurancePolicy>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getInsurancePolicies(status, policyType)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        Resource.Success(apiResponse.data)
                    } else {
                        Resource.Error(apiResponse.message ?: "Failed to fetch policies")
                    }
                } else {
                    Resource.Error("Failed to fetch policies")
                }
            } catch (e: Exception) {
                Timber.e(e, "Get insurance policies error")
                Resource.Error(e.message ?: "Network error occurred")
            }
        }
    }
}