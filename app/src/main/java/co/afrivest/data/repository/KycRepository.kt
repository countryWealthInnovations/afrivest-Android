package co.afrivest.data.repository

import co.afrivest.data.api.ApiService
import co.afrivest.data.api.KycSessionData
import co.afrivest.data.api.KycStatusData
import co.afrivest.data.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KycRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun createSession(): Resource<KycSessionData> = withContext(Dispatchers.IO) {
        try {
            val r = apiService.createKycSession()
            if (r.isSuccessful && r.body()?.success == true && r.body()?.data != null) {
                Resource.Success(r.body()!!.data)
            } else {
                Resource.Error(r.body()?.message ?: "Could not start verification")
            }
        } catch (e: Exception) {
            Timber.e(e, "KYC session failed")
            Resource.Error(e.message ?: "Could not start verification")
        }
    }

    suspend fun getStatus(): Resource<KycStatusData> = withContext(Dispatchers.IO) {
        try {
            val r = apiService.getKycStatus()
            if (r.isSuccessful && r.body()?.success == true && r.body()?.data != null) {
                Resource.Success(r.body()!!.data)
            } else {
                Resource.Error(r.body()?.message ?: "Could not fetch status")
            }
        } catch (e: Exception) {
            Timber.e(e, "KYC status failed")
            Resource.Error(e.message ?: "Could not fetch status")
        }
    }
}