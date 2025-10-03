package com.afrivest.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
data class Transaction(
    val id: Int,
    val reference: String,
    val type: String,
    val amount: String,
    val fee_amount: String? = null,
    val total_amount: String? = null,
    val currency: String,
    val status: String,
    val payment_channel: String? = null,
    val external_reference: String? = null,
    val description: String? = null,
    val user: TransactionUser? = null,
    val wallet: TransactionWallet? = null,
    val recipient: TransactionRecipient? = null,
    //val metadata: Map<String, Any>? = null,
    val created_at: String,
    val updated_at: String? = null,
    val completed_at: String? = null
) : Parcelable {
    fun getAmountDouble(): Double = amount.toDoubleOrNull() ?: 0.0
    fun getFeeDouble(): Double = fee_amount?.toDoubleOrNull() ?: 0.0
    fun getTotalDouble(): Double = total_amount?.toDoubleOrNull() ?: 0.0

    fun isPending(): Boolean = status == "pending"
    fun isSuccess(): Boolean = status == "success"
    fun isFailed(): Boolean = status == "failed"

    fun getStatusColor(): Int = when (status) {
        "success" -> android.graphics.Color.parseColor("#4CAF50")
        "pending" -> android.graphics.Color.parseColor("#FF9800")
        "failed" -> android.graphics.Color.parseColor("#F44336")
        else -> android.graphics.Color.parseColor("#9E9E9E")
    }

    fun getTypeDisplayName(): String = when (type) {
        "deposit" -> "Deposit"
        "withdrawal" -> "Withdrawal"
        "transfer" -> "Transfer"
        "insurance" -> "Insurance"
        "investment" -> "Investment"
        "bill_payment" -> "Bill Payment"
        "gold_purchase" -> "Gold Purchase"
        "crypto_purchase" -> "Crypto Purchase"
        else -> type.capitalize(Locale.ROOT)
    }
}

@Parcelize
data class TransactionUser(
    val id: Int,
    val name: String,
    val email: String
) : Parcelable

@Parcelize
data class TransactionWallet(
    val currency: String,
    val balance: String
) : Parcelable

@Parcelize
data class TransactionRecipient(
    val name: String,
    val email: String
) : Parcelable
