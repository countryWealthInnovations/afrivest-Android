package com.afrivest.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// MARK: - Withdraw Request
data class WithdrawRequest(
    val amount: Double,
    val currency: String,
    val network: String,
    val phone_number: String
)

// MARK: - Withdraw Response
@Parcelize
data class WithdrawResponse(
    val transaction_id: Int,
    val reference: String,
    val amount: String,
    val currency: String,
    val network: String,
    val status: String?
) : Parcelable