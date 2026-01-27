package com.afrivest.app.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileData(
    val id: Int,
    val name: String,
    val email: String,
    @SerializedName("phone_number")
    val phoneNumber: String?,
    val role: String,
    val status: String,
    @SerializedName("email_verified")
    val emailVerified: Boolean,
    @SerializedName("kyc_verified")
    val kycVerified: Boolean,
    @SerializedName("kyc_status")
    val kycStatus: String,
    @SerializedName("avatar_url")
    val avatarUrl: String?,
    @SerializedName("default_allocation_settings")
    val defaultAllocationSettings: AllocationSettings?,
    @SerializedName("investment_risk_profile")
    val investmentRiskProfile: String?,
    @SerializedName("created_at")
    val createdAt: String,
    val wallets: List<Wallet>,
    @SerializedName("investment_summary")
    val investmentSummary: InvestmentSummary?
) : Parcelable {

    fun isDefaultAvatar(): Boolean {
        return avatarUrl.isNullOrEmpty() ||
                avatarUrl == "https://afrivest.co/images/default-avatar.png"
    }

    fun getUserInitials(): String {
        val nameComponents = name.trim().split(" ")
        return when {
            nameComponents.size >= 2 -> {
                val firstInitial = nameComponents[0].firstOrNull()?.uppercaseChar() ?: ""
                val lastInitial = nameComponents[1].firstOrNull()?.uppercaseChar() ?: ""
                "$firstInitial$lastInitial"
            }
            nameComponents.size == 1 -> {
                nameComponents[0].firstOrNull()?.uppercaseChar()?.toString() ?: "U"
            }
            else -> "U"
        }
    }

    // Convert to User for backwards compatibility
    fun toUser(): User {
        return User(
            id = id,
            name = name,
            email = email,
            phone_number = phoneNumber ?: "",
            role = role,
            status = status,
            avatar_url = avatarUrl,
            email_verified = emailVerified,
            kyc_verified = kycVerified,
            created_at = createdAt,
            updated_at = null
        )
    }
}


@Parcelize
data class InvestmentSummary(
    @SerializedName("total_invested")
    val totalInvested: Double,
    @SerializedName("current_value")
    val currentValue: Double,
    @SerializedName("interest_earned")
    val interestEarned: Double,
    @SerializedName("interest_percentage")
    val interestPercentage: Double,
    @SerializedName("active_investments_count")
    val activeInvestmentsCount: Int
) : Parcelable {
    fun hasInvestments(): Boolean = activeInvestmentsCount > 0

    fun getFormattedCurrentValue(): String = String.format("%,.2f", currentValue)

    fun getFormattedReturns(): String = String.format("%,.2f", interestEarned)

    fun getFormattedPercentage(): String = String.format("%.2f%%", interestPercentage)

    fun isPositiveReturn(): Boolean = interestEarned >= 0
}

@Parcelize
data class AllocationSettings(
    val p2p: Int,
    val insurance: Int,
    val investment: Int
) : Parcelable