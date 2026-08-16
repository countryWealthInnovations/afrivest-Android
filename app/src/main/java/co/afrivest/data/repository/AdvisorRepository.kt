package co.afrivest.data.repository

import co.afrivest.data.api.ApiResponse
import co.afrivest.data.api.ApiService
import co.afrivest.data.model.*
import co.afrivest.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdvisorRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun list(): Resource<List<Advisor>> = call { apiService.getAdvisors() }
    suspend fun detail(id: Int): Resource<Advisor> = call { apiService.getAdvisor(id) }
    suspend fun myBookings(): Resource<List<AdvisorBookingDto>> = call { apiService.getMyBookings() }
    suspend fun dashboard(): Resource<AdvisorDashboard> = call { apiService.getAdvisorDashboard() }

    suspend fun book(id: Int, scheduledAt: String, notes: String?): Resource<AdvisorBookingDto> =
        withContext(Dispatchers.IO) {
            try {
                val r = apiService.bookAdvisor(id, BookAdvisorBody(scheduledAt, notes))
                if (r.isSuccessful && r.body()?.success == true && r.body()?.data != null) {
                    Resource.Success(r.body()!!.data.booking)
                } else {
                    Resource.Error(r.body()?.message ?: "Booking failed")
                }
            } catch (e: Exception) {
                Timber.e(e, "Book advisor failed")
                Resource.Error(e.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
            }
        }

    suspend fun updateProfile(title: String?, bio: String?, expertise: String?): Resource<Boolean> =
        ok { apiService.updateAdvisorProfile(UpdateAdvisorProfileBody(title, bio, expertise)) }

    suspend fun setAvailability(slots: List<AvailabilitySlot>): Resource<Boolean> =
        ok { apiService.updateAdvisorAvailability(UpdateAvailabilityBody(slots)) }

    suspend fun blockDate(date: String, reason: String?): Resource<Boolean> =
        ok { apiService.blockAdvisorDate(BlockDateBody(date, reason)) }

    suspend fun unblockDate(id: Int): Resource<Boolean> =
        ok { apiService.unblockAdvisorDate(id) }

    private suspend fun <T> call(block: suspend () -> Response<ApiResponse<T>>): Resource<T> =
        withContext(Dispatchers.IO) {
            try {
                val r = block()
                if (r.isSuccessful && r.body()?.success == true && r.body()?.data != null) {
                    Resource.Success(r.body()!!.data)
                } else {
                    Resource.Error(r.body()?.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
                }
            } catch (e: Exception) {
                Timber.e(e, "Advisor call failed")
                Resource.Error(e.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
            }
        }

    private suspend fun ok(block: suspend () -> Response<ApiResponse<Unit>>): Resource<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val r = block()
                if (r.isSuccessful && r.body()?.success == true) Resource.Success(true)
                else Resource.Error(r.body()?.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
            } catch (e: Exception) {
                Timber.e(e, "Advisor update failed")
                Resource.Error(e.message ?: Constants.ErrorMessages.UNKNOWN_ERROR)
            }
        }
}