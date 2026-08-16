package co.afrivest.data.model

data class Advisor(
    val id: Int,
    val display_name: String,
    val title: String?,
    val expertise: String?,
    val booking_fee: String?,
    val booking_fee_currency: String?,
    val session_duration_minutes: Int?,
    val avatar_url: String?,
    val bio: String? = null,
    val slots: List<AdvisorSlot>? = null
)

data class AdvisorSlot(
    val date: String,
    val start: String,
    val end: String,
    val datetime: String
)

data class AdvisorBookingDto(
    val uuid: String,
    val reference: String,
    val scheduled_at: String,
    val duration_minutes: Int,
    val fee_paid: String?,
    val fee_currency: String?,
    val status: String,
    val meeting_link: String?,
    val advisor: AdvisorMini? = null,
    val client_name: String? = null,
    val notes: String? = null
)

data class AdvisorMini(val id: Int, val display_name: String, val title: String?)

data class BookAdvisorBody(val scheduled_at: String, val notes: String?)
data class BookingWrapper(val booking: AdvisorBookingDto)

// Advisor side
data class AdvisorDashboard(
    val profile: AdvisorSelfProfile,
    val availability: List<AvailabilitySlot>,
    val date_blocks: List<DateBlock>,
    val upcoming_bookings: List<AdvisorBookingDto>
)

data class AdvisorSelfProfile(
    val id: Int,
    val display_name: String,
    val title: String?,
    val bio: String?,
    val expertise: String?,
    val booking_fee: String?,
    val booking_fee_currency: String?,
    val session_duration_minutes: Int?,
    val is_active: Boolean
)

data class AvailabilitySlot(
    val id: Int? = null,
    val day_of_week: Int,
    val start_time: String,
    val end_time: String,
    val is_active: Boolean = true
)

data class DateBlock(val id: Int, val date: String, val reason: String?)

data class UpdateAvailabilityBody(val slots: List<AvailabilitySlot>)
data class UpdateAdvisorProfileBody(val title: String?, val bio: String?, val expertise: String?)
data class BlockDateBody(val date: String, val reason: String?)