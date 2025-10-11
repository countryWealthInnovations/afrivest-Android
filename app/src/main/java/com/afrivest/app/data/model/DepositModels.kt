package com.afrivest.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


// REQUEST MODELS
data class MobileMoneyDepositRequest(
    val amount: Double,
    val currency: String,
    val payment_method: String,
    val payment_provider: String,
    val phone_number: String
)

data class CardDepositRequest(
    val amount: Double,
    val currency: String,
    val card_number: String,
    val cvv: String,
    val expiry_month: String,
    val expiry_year: String
)

data class BankTransferRequest(
    val amount: Double,
    val currency: String
)