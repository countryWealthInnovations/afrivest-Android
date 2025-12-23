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

    // ==================== INVESTMENTS ====================

    @GET("investment-categories")
    suspend fun getInvestmentCategories(): Response<ApiResponse<List<InvestmentCategory>>>

    @GET("investment-categories/{slug}")
    suspend fun getInvestmentCategory(
        @Path("slug") slug: String
    ): Response<ApiResponse<InvestmentCategory>>

    @GET("investment-products")
    suspend fun getInvestmentProducts(
        @Query("category_slug") categorySlug: String? = null,
        @Query("risk_level") riskLevel: String? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("per_page") perPage: Int = 20
    ): Response<ApiResponse<List<InvestmentProduct>>>

    @GET("investment-products/featured")
    suspend fun getFeaturedInvestmentProducts(): Response<ApiResponse<List<InvestmentProduct>>>

    @GET("investment-products/{slug}")
    suspend fun getInvestmentProduct(
        @Path("slug") slug: String
    ): Response<ApiResponse<InvestmentProduct>>

    @POST("investments")
    suspend fun purchaseInvestment(
        @Body request: PurchaseInvestmentRequest
    ): Response<ApiResponse<UserInvestment>>

    @GET("investments")
    suspend fun getUserInvestments(
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<UserInvestment>>>

    @GET("investments/{id}")
    suspend fun getUserInvestment(
        @Path("id") id: Int
    ): Response<ApiResponse<UserInvestment>>

    // ==================== INSURANCE ====================

    @GET("insurance-policies/providers")
    suspend fun getInsuranceProviders(): Response<ApiResponse<List<InsuranceProvider>>>

    @POST("insurance-policies")
    suspend fun purchaseInsurancePolicy(
        @Body request: PurchaseInsurancePolicyRequest
    ): Response<ApiResponse<InsurancePolicy>>

    @GET("insurance-policies")
    suspend fun getInsurancePolicies(
        @Query("status") status: String? = null,
        @Query("policy_type") policyType: String? = null
    ): Response<ApiResponse<List<InsurancePolicy>>>

    @GET("insurance-policies/{id}")
    suspend fun getInsurancePolicy(
        @Path("id") id: Int
    ): Response<ApiResponse<InsurancePolicy>>

    // ==================== GOLD MARKETPLACE ====================

    @GET("investment-products")
    suspend fun getGoldProducts(
        @Query("category_slug") categorySlug: String = "gold"
    ): Response<ApiResponse<List<InvestmentProduct>>>

    @GET("gold/current-price")
    suspend fun getCurrentGoldPrice(): Response<ApiResponse<GoldPrice>>

    // ==================== TRANSFERS ====================

    @POST(Constants.Endpoints.P2P_TRANSFER)
    suspend fun transferP2P(
        @Body request: P2PTransferRequest
    ): Response<ApiResponse<P2PTransferResponse>>

    @GET("users/search")
    suspend fun searchUser(
        @Query("query") query: String
    ): Response<ApiResponse<UserSearchResponse>>

    // ==================== WITHDRAWALS ====================

    @POST(Constants.Endpoints.WITHDRAW_MOBILE_MONEY)
    suspend fun withdrawMobileMoney(
        @Body request: WithdrawRequest
    ): Response<ApiResponse<WithdrawResponse>>
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

// ==================== INVESTMENT MODELS ====================

data class InvestmentCategory(
    val name: String,
    val slug: String,
    val icon: String?
)

data class InvestmentProduct(
    val id: Int,
    val title: String,
    val slug: String,
    val short_description: String?,
    val featured_image: String?,
    val price: String,
    val currency: String,
    val min_investment: String,
    val min_investment_formatted: String,
    val expected_returns: String,
    val risk_level: String,
    val risk_level_label: String,
    val duration_label: String,
    val availability_status: String,
    val is_featured: Boolean,
    val rating_average: String,
    val rating_count: Int,
    val category: InvestmentCategory?,
    val partner: InvestmentPartner?
) {
    // Compatibility properties
    val name: String get() = title
    val description: String? get() = short_description
    val image_url: String? get() = featured_image
    val minimum_investment: String get() = min_investment_formatted
}

data class InvestmentPartner(
    val name: String,
    val logo: String?
) {
    val id: Int? = null // For compatibility with filter logic
}

data class PurchaseInvestmentRequest(
    val product_id: Int,
    val amount: Double,
    val currency: String,
    val payout_frequency: String?,
    val auto_reinvest: Boolean?
)

data class UserInvestment(
    val id: Int,
    val user_id: Int,
    val product_id: Int,
    val product: InvestmentProduct?,
    val amount: String,
    val currency: String,
    val status: String,
    val purchase_date: String,
    val maturity_date: String?,
    val current_value: String,
    val returns_earned: String,
    val payout_frequency: String?,
    val auto_reinvest: Boolean,
    val created_at: String
)

// ==================== INSURANCE MODELS ====================

data class InsuranceProvider(
    val id: Int,
    val name: String,
    val logo_url: String?,
    val description: String?
)

data class PurchaseInsurancePolicyRequest(
    val partner_id: Int,
    val policy_type: String,
    val coverage_amount: Double,
    val premium_amount: Double,
    val premium_frequency: String,
    val beneficiaries: List<Beneficiary>,
    val start_date: String,
    val end_date: String,
    val auto_deduct_wallet: Boolean?,
    val wallet_id: Int?
)

data class Beneficiary(
    val name: String,
    val relationship: String,
    val percentage: Int
)

data class InsurancePolicy(
    val id: Int,
    val user_id: Int,
    val partner_id: Int,
    val partner: InsuranceProvider?,
    val policy_number: String,
    val policy_type: String,
    val coverage_amount: String,
    val premium_amount: String,
    val premium_frequency: String,
    val status: String,
    val start_date: String,
    val end_date: String,
    val beneficiaries: List<Beneficiary>,
    val created_at: String
)

// ==================== GOLD MODELS ====================

data class GoldPrice(
    val price_per_gram_usd: Double,
    val price_per_gram_ugx: Double,
    val last_updated: String
)
@Parcelize
data class ErrorDetails(
    val error_code: String,
    val message: String,
    val action: String?,
    val can_retry: Boolean,
    val severity: String
) : Parcelable