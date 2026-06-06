package co.afrivest.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class WithdrawRequest(
    val amount: Double,
    val currency: String,
    val wallet_currency: String,
    val network: String,
    val phone_number: String
)

data class BankWithdrawRequest(
    val amount: Double,
    val currency: String,
    val wallet_currency: String,
    val bank_code: String,
    val account_number: String,
    val account_name: String
)

@Parcelize
data class WithdrawResponse(
    val transaction_id: Int,
    val reference: String,
    val amount: String,
    val total_fee: Double? = null,
    val total_debited: Double? = null,
    val currency: String,
    val wallet_currency: String? = null,
    val network: String? = null,
    val status: String? = null
) : Parcelable