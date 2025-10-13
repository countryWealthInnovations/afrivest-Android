package com.afrivest.app.data.api

import android.os.Parcelable
import com.afrivest.app.data.model.*
import com.afrivest.app.utils.Constants
import com.afrivest.app.data.model.User
import kotlinx.parcelize.Parcelize
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==================== AUTHENTICATION ====================

    @POST(Constants.Endpoints.REGISTER)
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<AuthResponse>>

    @POST(Constants.Endpoints.LOGIN)
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<AuthResponse>>

    @POST(Constants.Endpoints.LOGOUT)
    suspend fun logout(): Response<ApiResponse<Unit>>

    @GET(Constants.Endpoints.ME)
    suspend fun getCurrentUser(): Response<ApiResponse<User>>

    @POST(Constants.Endpoints.VERIFY_OTP)
    suspend fun verifyOTP(
        @Body request: OTPRequest
    ): Response<ApiResponse<OTPResponse>>

    @POST(Constants.Endpoints.RESEND_OTP)
    suspend fun resendOTP(): Response<ApiResponse<OTPResponse>>

    @POST(Constants.Endpoints.FORGOT_PASSWORD)
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<ApiResponse<Unit>>

    @POST(Constants.Endpoints.RESET_PASSWORD)
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ApiResponse<Unit>>


    // ==================== PROFILE ====================

    @GET(Constants.Endpoints.PROFILE)
    suspend fun getProfile(): Response<ApiResponse<ProfileData>>

    @PUT(Constants.Endpoints.PROFILE)
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<User>>

    @PUT(Constants.Endpoints.UPDATE_PASSWORD)
    suspend fun updatePassword(
        @Body request: UpdatePasswordRequest
    ): Response<ApiResponse<Unit>>

    @Multipart
    @POST(Constants.Endpoints.UPLOAD_AVATAR)
    suspend fun uploadAvatar(
        @Part avatar: MultipartBody.Part
    ): Response<ApiResponse<AvatarResponse>>

    @DELETE(Constants.Endpoints.DELETE_AVATAR)
    suspend fun deleteAvatar(): Response<ApiResponse<Unit>>


    // ==================== WALLETS ====================

    @GET(Constants.Endpoints.WALLETS)
    suspend fun getWallets(): Response<ApiResponse<List<Wallet>>>

    @GET
    suspend fun getWallet(
        @Url url: String
    ): Response<ApiResponse<Wallet>>

    @GET
    suspend fun getWalletTransactions(
        @Url url: String,
        @Query("per_page") perPage: Int = 15,
        @Query("page") page: Int = 1
    ): Response<ApiResponse<PaginatedResponse<Transaction>>>


    // ==================== DEPOSITS ====================


    @POST("deposits/mobile-money")
    suspend fun depositMobileMoney(@Body request: MobileMoneyDepositRequest): Response<ApiResponse<DepositResponse>>

    @POST("deposits/bank-transfer")
    suspend fun depositBankTransfer(@Body request: BankTransferRequest): Response<ApiResponse<DepositResponse>>

    @GET("deposits/{id}/check")
    suspend fun checkDepositStatus(@Path("id") id: String): Response<ApiResponse<TransactionStatus>>

    @GET("deposits/{reference}/status")
    suspend fun getDepositStatus(@Path("reference") reference: String): Response<ApiResponse<TransactionStatus>>

    @POST(Constants.Endpoints.DEPOSIT_CARD)
    suspend fun depositCard(@Body request: CardDepositRequest): Response<ApiResponse<DepositResponse>>



    // ==================== WITHDRAWALS ====================

    @POST(Constants.Endpoints.WITHDRAW_BANK)
    suspend fun withdrawBank(
        @Body request: BankWithdrawalRequest
    ): Response<ApiResponse<WithdrawalResponse>>

    @POST(Constants.Endpoints.WITHDRAW_MOBILE_MONEY)
    suspend fun withdrawMobileMoney(
        @Body request: MobileMoneyWithdrawalRequest
    ): Response<ApiResponse<WithdrawalResponse>>

    @GET
    suspend fun getWithdrawalStatus(
        @Url url: String
    ): Response<ApiResponse<TransactionStatus>>


    // ==================== TRANSFERS ====================

    @POST(Constants.Endpoints.P2P_TRANSFER)
    suspend fun p2pTransfer(
        @Body request: P2PTransferRequest
    ): Response<ApiResponse<TransferResponse>>

    @POST(Constants.Endpoints.INSURANCE)
    suspend fun insurancePurchase(
        @Body request: InsuranceRequest
    ): Response<ApiResponse<TransferResponse>>

    @POST(Constants.Endpoints.INVESTMENT)
    suspend fun investment(
        @Body request: InvestmentRequest
    ): Response<ApiResponse<TransferResponse>>

    @POST(Constants.Endpoints.BILL_PAYMENT)
    suspend fun billPayment(
        @Body request: BillPaymentRequest
    ): Response<ApiResponse<TransferResponse>>

    @POST(Constants.Endpoints.GOLD)
    suspend fun goldPurchase(
        @Body request: GoldPurchaseRequest
    ): Response<ApiResponse<TransferResponse>>

    @POST(Constants.Endpoints.CRYPTO)
    suspend fun cryptoPurchase(
        @Body request: CryptoPurchaseRequest
    ): Response<ApiResponse<TransferResponse>>


    // ==================== TRANSACTIONS ====================

    @GET(Constants.Endpoints.TRANSACTIONS)
    suspend fun getTransactions(
        @Query("per_page") perPage: Int = 15,
        @Query("page") page: Int = 1,
        @Query("type") type: String? = null,
        @Query("status") status: String? = null,
        @Query("currency") currency: String? = null
    ): Response<ApiResponse<List<Transaction>>>

    @GET
    suspend fun getTransaction(
        @Url url: String
    ): Response<ApiResponse<Transaction>>

    @GET
    suspend fun getTransactionReceipt(
        @Url url: String
    ): Response<ApiResponse<TransactionReceipt>>


    // ==================== FOREX ====================

    @GET(Constants.Endpoints.FOREX_RATES)
    suspend fun getForexRates(): Response<ApiResponse<ForexRates>>

    @GET(Constants.Endpoints.FOREX_CONVERT)
    suspend fun convertCurrency(
        @Query("amount") amount: Double,
        @Query("from") from: String,
        @Query("to") to: String
    ): Response<ApiResponse<CurrencyConversion>>


    // ==================== DASHBOARD ====================

    @GET(Constants.Endpoints.DASHBOARD)
    suspend fun getDashboard(): Response<ApiResponse<Dashboard>>
}

// ==================== REQUEST MODELS ====================

data class RegisterRequest(
    val name: String,
    val email: String,
    val phone_number: String,
    val password: String,
    val password_confirmation: String,
    val device_token: String? = null,
    val device_type: String? = "android",
    val device_name: String? = null,
    val app_version: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String,
    val device_token: String? = null,
    val device_type: String? = "android",
    val device_name: String? = null
)

data class OTPRequest(
    val code: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val password: String,
    val password_confirmation: String
)

data class UpdateProfileRequest(
    val name: String? = null,
    val phone_number: String? = null
)

data class UpdatePasswordRequest(
    val current_password: String,
    val new_password: String,
    val new_password_confirmation: String
)

data class CardDepositRequest(
    val amount: Double,
    val currency: String,
    val card_number: String,
    val cvv: String,
    val expiry_month: String,
    val expiry_year: String
)

data class MobileMoneyDepositRequest(
    val amount: Double,
    val currency: String,
    val payment_method: String = "mobile_money",
    val payment_provider: String,
    val phone_number: String
)

data class BankDepositRequest(
    val amount: Double,
    val currency: String
)

data class BankWithdrawalRequest(
    val amount: Double,
    val currency: String,
    val withdrawal_method: String = "bank",
    val bank_name: String,
    val account_number: String,
    val account_name: String
)

data class MobileMoneyWithdrawalRequest(
    val amount: Double,
    val currency: String,
    val withdrawal_method: String = "mobile_money",
    val withdrawal_provider: String,
    val phone_number: String
)

data class P2PTransferRequest(
    val recipient_email: String,
    val amount: Double,
    val currency: String,
    val description: String? = null
)

data class InsuranceRequest(
    val amount: Double,
    val currency: String,
    val policy_type: String,
    val coverage_details: Map<String, Any>? = null
)

data class InvestmentRequest(
    val amount: Double,
    val currency: String,
    val investment_type: String,
    val investment_name: String
)

data class BillPaymentRequest(
    val amount: Double,
    val currency: String,
    val bill_type: String,
    val provider: String,
    val account_number: String
)

data class GoldPurchaseRequest(
    val amount: Double,
    val currency: String,
    val quantity_grams: Double,
    val delivery_method: String
)

data class CryptoPurchaseRequest(
    val amount: Double,
    val currency: String,
    val crypto_currency: String,
    val crypto_amount: Double
)

@Parcelize
data class DepositResponse(
    val transaction_id: Int,
    val reference: String,
    val amount: String,
    val currency: String,
    val status: String? = null,
    val network: String? = null,
    val payment_data: PaymentData
) : Parcelable

@Parcelize
data class PaymentData(
    val mode: String,
    val url: String? = null,
    val authorization_url: String? = null,
    val redirect_url: String? = null,
    val flutterwave_transaction_id: String? = null
) : Parcelable {
    val paymentUrl: String?
        get() = url ?: authorization_url ?: redirect_url
}

@Parcelize
data class TransactionStatus(
    val transaction_id: Int,
    val reference: String,
    val amount: String,
    val currency: String,
    val status: String,
    val payment_method: String,
    val network: String? = null,
    val created_at: String,
    val updated_at: String,
    val message: String? = null,
    val error: ErrorDetails? = null
) : Parcelable

@Parcelize
data class ErrorDetails(
    val error_code: String,
    val message: String,
    val action: String?,
    val can_retry: Boolean,
    val severity: String
) : Parcelable