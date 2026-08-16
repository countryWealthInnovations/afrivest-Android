package co.afrivest.data.repository

import co.afrivest.data.api.ApiService
import co.afrivest.data.api.QrData
import co.afrivest.data.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QrRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getMyQr(): Resource<QrData> = withContext(Dispatchers.IO) {
        try {
            val r = apiService.getMyQr()
            if (r.isSuccessful && r.body()?.success == true && r.body()?.data != null) {
                Resource.Success(r.body()!!.data)
            } else {
                Resource.Error(r.body()?.message ?: "Could not load your QR code")
            }
        } catch (e: Exception) {
            Timber.e(e, "QR fetch failed")
            Resource.Error(e.message ?: "Could not load your QR code")
        }
    }
}