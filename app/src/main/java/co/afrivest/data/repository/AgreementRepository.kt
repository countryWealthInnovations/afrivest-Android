package co.afrivest.data.repository

import co.afrivest.data.api.ApiService
import co.afrivest.data.api.InvestmentAgreementData
import co.afrivest.data.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgreementRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getAgreement(): Resource<InvestmentAgreementData> = withContext(Dispatchers.IO) {
        try {
            val r = apiService.getInvestmentAgreement()
            if (r.isSuccessful && r.body()?.success == true && r.body()?.data != null) {
                Resource.Success(r.body()!!.data)
            } else {
                Resource.Error(r.body()?.message ?: "Could not load agreement")
            }
        } catch (e: Exception) {
            Timber.e(e, "Agreement load failed")
            Resource.Error(e.message ?: "Could not load agreement")
        }
    }

    suspend fun accept(): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val r = apiService.acceptInvestmentAgreement()
            if (r.isSuccessful && r.body()?.success == true) {
                Resource.Success(true)
            } else {
                Resource.Error(r.body()?.message ?: "Could not accept agreement")
            }
        } catch (e: Exception) {
            Timber.e(e, "Agreement accept failed")
            Resource.Error(e.message ?: "Could not accept agreement")
        }
    }
}