package com.afrivest.app.data.repository

import com.afrivest.app.data.api.ApiResponse
import com.afrivest.app.data.api.ApiService
import com.afrivest.app.data.local.SecurePreferences
import com.afrivest.app.data.model.ProfileData
import com.afrivest.app.data.model.Resource
import timber.log.Timber
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val apiService: ApiService,
    private val securePreferences: SecurePreferences
) {

    /**
     * Get profile with caching strategy
     * 1. Return cached data immediately if available
     * 2. Fetch fresh data from API
     * 3. Update cache with fresh data
     */
    suspend fun getProfile(): Resource<ProfileData> {
        return try {
            // Check cache first
            val cachedProfile = securePreferences.getCachedProfile()

            // If cache exists, emit it first
            if (cachedProfile != null) {
                Timber.d("✅ Returning cached profile")
            }

            // Fetch fresh data from API
            val response = apiService.getProfile()

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    val profileData = body.data

                    // Save to cache
                    securePreferences.saveProfile(profileData)

                    // Update KYC status
                    securePreferences.setKYCVerified(profileData.kycVerified)

                    Timber.d("✅ Profile fetched and cached")
                    Resource.Success(profileData)
                } else {
                    Timber.e("❌ Profile API error: ${body?.message}")

                    // Return cached data if API fails
                    if (cachedProfile != null) {
                        Resource.Success(cachedProfile)
                    } else {
                        Resource.Error(body?.message ?: "Failed to load profile")
                    }
                }
            } else {
                Timber.e("❌ Profile request failed: ${response.code()}")

                // Return cached data if API fails
                if (cachedProfile != null) {
                    Resource.Success(cachedProfile)
                } else {
                    Resource.Error("Failed to load profile: ${response.message()}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Profile exception")

            // Return cached data if exception occurs
            val cachedProfile = securePreferences.getCachedProfile()
            if (cachedProfile != null) {
                Resource.Success(cachedProfile)
            } else {
                Resource.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    /**
     * Force refresh profile (clears cache and fetches fresh)
     */
    suspend fun forceRefreshProfile(): Resource<ProfileData> {
        return try {
            securePreferences.clearProfile()

            val response = apiService.getProfile()

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    val profileData = body.data

                    // Save to cache
                    securePreferences.saveProfile(profileData)
                    securePreferences.setKYCVerified(profileData.kycVerified)

                    Timber.d("✅ Profile force refreshed")
                    Resource.Success(profileData)
                } else {
                    Resource.Error(body?.message ?: "Failed to refresh profile")
                }
            } else {
                Resource.Error("Failed to refresh profile: ${response.message()}")
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Profile force refresh exception")
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }
}