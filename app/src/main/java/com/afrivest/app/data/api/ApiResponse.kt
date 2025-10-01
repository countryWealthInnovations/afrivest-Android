package com.afrivest.app.data.api

import com.afrivest.app.data.model.Transaction
import com.afrivest.app.data.model.User


data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T
)

data class PaginatedResponse<T>(
    val current_page: Int,
    val data: List<T>,
    val per_page: Int,
    val total: Int,
    val last_page: Int? = null
)

// ==================== RESPONSE MODELS ====================

data class AuthResponse(
    val user: User,
    val token: String,
    val otp_sent: Boolean? = null,
    val otp_channel: String? = null
)

data class OTPResponse(
    val otp_sent: Boolean,
    val otp_channel: String,
    val expires_in: Int
)

data class AvatarResponse(
    val avatar_url: String
)

data class DepositResponse(
    val transaction: Transaction,
    val payment_data: PaymentData? = null,
    val bank_details: BankDetails? = null,
    val instructions: String? = null
)

data class PaymentData(
    val authorization_url: String? = null,
    val tx_ref: String? = null
)

data class BankDetails(
    val account_name: String,
    val account_number: String,
    val bank_name: String,
    val reference: String
)

data class WithdrawalResponse(
    val transaction: Transaction,
    val estimated_completion: String? = null
)

data class TransferResponse(
    val transaction: Transaction,
    val recipient: RecipientInfo? = null
)

data class RecipientInfo(
    val name: String,
    val email: String
)

data class TransactionStatus(
    val reference: String,
    val status: String,
    val amount: String,
    val currency: String,
    val created_at: String,
    val completed_at: String?
)

data class TransactionReceipt(
    val receipt: Receipt
)

data class Receipt(
    val transaction_id: Int,
    val reference: String,
    val date: String,
    val type: String,
    val amount: String,
    val fee: String,
    val total: String,
    val status: String,
    val payment_method: String?,
    val user: ReceiptUser,
    val company: ReceiptCompany
)

data class ReceiptUser(
    val name: String,
    val email: String
)

data class ReceiptCompany(
    val name: String,
    val email: String,
    val phone: String
)

data class ForexRates(
    val base_currency: String,
    val rates: Map<String, Double>,
    val updated_at: String,
    val markup_percentage: Double
)

data class CurrencyConversion(
    val from: CurrencyAmount,
    val to: CurrencyAmount,
    val rate: Double,
    val markup_percentage: Double,
    val converted_at: String
)

data class CurrencyAmount(
    val currency: String,
    val amount: Double
)