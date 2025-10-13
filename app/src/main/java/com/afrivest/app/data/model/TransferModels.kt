package com.afrivest.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// MARK: - P2P Transfer Request
data class P2PTransferRequest(
    val recipient_id: Int,
    val amount: Double,
    val currency: String,
    val description: String?
)

// MARK: - P2P Transfer Response
@Parcelize
data class P2PTransferResponse(
    val transaction: TransferTransaction
) : Parcelable

@Parcelize
data class TransferTransaction(
    val id: Int,
    val reference: String,
    val amount: String,
    val fee: String,
    val total: Double,
    val currency: String,
    val status: String,
    val sender: String,
    val recipient: String,
    val created_at: String
) : Parcelable

// MARK: - Contact Model
data class AppContact(
    val id: String, // Local ID for list
    val name: String,
    val phoneNumber: String?,
    val email: String?,
    val userId: Int?, // Backend user ID
    val isRegistered: Boolean
) {
    fun getDisplayIdentifier(): String {
        return phoneNumber ?: email ?: "Unknown"
    }
}

// MARK: - User Search Response
data class UserSearchResponse(
    val found: Boolean,
    val user: SearchedUser?
)

data class SearchedUser(
    val id: Int,
    val name: String,
    val email: String,
    val phone_number: String?
)