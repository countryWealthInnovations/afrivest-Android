package co.afrivest.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val phone_number: String,
    val role: String,
    val status: String,
    val avatar_url: String? = null,
    val email_verified: Boolean = false,
    val kyc_verified: Boolean = false,
    val default_currency: String? = null,
    val secondary_currency: String? = null,
    val requires_currency_setup: Boolean? = null,
    val kyc_banner_hidden: Boolean? = false,
    val created_at: String,
    val updated_at: String? = null
) : Parcelable {
    fun isActive(): Boolean = status == "active"
    fun isEmailVerified(): Boolean = email_verified
    fun isKYCVerified(): Boolean = kyc_verified
}