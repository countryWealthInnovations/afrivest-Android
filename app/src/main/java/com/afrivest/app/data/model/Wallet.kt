package com.afrivest.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Wallet(
    val id: Int? = null,
    val currency: String,
    val balance: String,
    val status: String,
    val wallet_type: String? = null,
    val formatted_balance: String? = null,
    val last_transaction_at: String? = null,
    val total_incoming: String? = null,
    val total_outgoing: String? = null,
    val created_at: String? = null
) : Parcelable {
    fun getBalanceDouble(): Double = balance.toDoubleOrNull() ?: 0.0
    fun isActive(): Boolean = status == "active"

    fun getCurrencySymbol(): String = when (currency) {
        "UGX" -> "UGX"
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        else -> currency
    }
}