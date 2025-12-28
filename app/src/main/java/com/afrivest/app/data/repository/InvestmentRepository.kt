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
class InvestmentRepository @Inject constructor(
    private val apiService: ApiService,
    private val securePreferences: SecurePreferences
) {

    suspend fun getInvestmentCategories(): Resource<List<InvestmentCategory>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getInvestmentCategories()

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        Resource.Success(apiResponse.data)
                    } else {
                        Resource.Error(apiResponse.message ?: "Failed to fetch categories")
                    }
                } else {
                    Resource.Error("Failed to fetch categories")
                }
            } catch (e: Exception) {
                Timber.e(e, "Get investment categories error")
                Resource.Error(e.message ?: "Network error occurred")
            }
        }
    }

    suspend fun getInvestmentProducts(
        categorySlug: String? = null,
        riskLevel: String? = null,
        sortBy: String? = null
    ): Resource<List<InvestmentProduct>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getInvestmentProducts(
                    categorySlug = categorySlug,
                    riskLevel = riskLevel,
                    sortBy = sortBy
                )

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        Resource.Success(apiResponse.data)
                    } else {
                        Resource.Error(apiResponse.message ?: "Failed to fetch products")
                    }
                } else {
                    Resource.Error("Failed to fetch products")
                }
            } catch (e: Exception) {
                Timber.e(e, "Get investment products error")
                Resource.Error(e.message ?: "Network error occurred")
            }
        }
    }

    suspend fun getFeaturedProducts(): Resource<List<InvestmentProduct>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getFeaturedInvestmentProducts()

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        Resource.Success(apiResponse.data)
                    } else {
                        Resource.Error(apiResponse.message ?: "Failed to fetch featured products")
                    }
                } else {
                    Resource.Error("Failed to fetch featured products")
                }
            } catch (e: Exception) {
                Timber.e(e, "Get featured products error")
                Resource.Error(e.message ?: "Network error occurred")
            }
        }
    }

    suspend fun getInvestmentProduct(slug: String): Resource<InvestmentProduct> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getInvestmentProduct(slug)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        Resource.Success(apiResponse.data)
                    } else {
                        Resource.Error(apiResponse.message ?: "Failed to fetch product")
                    }
                } else {
                    Resource.Error("Failed to fetch product")
                }
            } catch (e: Exception) {
                Timber.e(e, "Get investment product error")
                Resource.Error(e.message ?: "Network error occurred")
            }
        }
    }

    suspend fun purchaseInvestment(request: PurchaseInvestmentRequest): Resource<UserInvestment> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.purchaseInvestment(request)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        Resource.Success(apiResponse.data)
                    } else {
                        Resource.Error(apiResponse.message ?: "Purchase failed")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = try {
                        val json = org.json.JSONObject(errorBody ?: "{}")
                        when {
                            json.has("errors") -> {
                                val errors = json.getJSONObject("errors")
                                val firstKey = errors.keys().next()
                                val errorArray = errors.getJSONArray(firstKey)
                                errorArray.getString(0)
                            }
                            json.has("message") -> json.getString("message")
                            else -> "Purchase failed"
                        }
                    } catch (e: Exception) {
                        "Purchase failed"
                    }
                    Resource.Error(errorMsg)
                }
            } catch (e: Exception) {
                Timber.e(e, "Purchase investment error")
                Resource.Error(e.message ?: "Network error occurred")
            }
        }
    }

    suspend fun getUserInvestments(status: String? = null): Resource<List<UserInvestment>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserInvestments(status)

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        Resource.Success(apiResponse.data)
                    } else {
                        Resource.Error(apiResponse.message ?: "Failed to fetch investments")
                    }
                } else {
                    Resource.Error("Failed to fetch investments")
                }
            } catch (e: Exception) {
                Timber.e(e, "Get user investments error")
                Resource.Error(e.message ?: "Network error occurred")
            }
        }
    }
}