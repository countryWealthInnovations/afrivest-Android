package com.afrivest.app.utils

object Constants {

    // API Configuration
    const val BASE_URL = "https://afrivest.countrywealth.ug/api/"
    const val TIMEOUT_CONNECT = 30L // seconds
    const val TIMEOUT_READ = 30L // seconds
    const val TIMEOUT_WRITE = 30L // seconds

    // API Endpoints
    object Endpoints {
        // Authentication
        const val REGISTER = "auth/register"
        const val LOGIN = "auth/login"
        const val LOGOUT = "auth/logout"
        const val ME = "auth/me"
        const val VERIFY_OTP = "auth/verify-otp"
        const val RESEND_OTP = "auth/resend-otp"
        const val FORGOT_PASSWORD = "auth/forgot-password"
        const val RESET_PASSWORD = "auth/reset-password"

        // Profile
        const val PROFILE = "profile"
        const val UPDATE_PASSWORD = "profile/password"
        const val UPLOAD_AVATAR = "profile/avatar"
        const val DELETE_AVATAR = "profile/avatar"

        // Wallets
        const val WALLETS = "wallets"
        fun wallet(currency: String) = "wallets/$currency"
        fun walletTransactions(currency: String) = "wallets/$currency/transactions"

        // Deposits
        const val DEPOSITS = "/deposits"
        const val DEPOSIT_CARD = "deposits/card"
        const val DEPOSIT_MOBILE_MONEY = "deposits/mobile-money"
        const val DEPOSIT_BANK = "deposits/bank-transfer"
        fun depositStatus(reference: String) = "deposits/$reference/status"

        // Withdrawals
        const val WITHDRAW_BANK = "withdrawals/bank"
        const val WITHDRAW_MOBILE_MONEY = "withdrawals/mobile-money"
        fun withdrawalStatus(reference: String) = "withdrawals/$reference/status"

        // Transfers
        const val P2P_TRANSFER = "transfers/p2p"
        const val INSURANCE = "transfers/insurance"
        const val INVESTMENT = "transfers/investment"
        const val BILL_PAYMENT = "transfers/bill-payment"
        const val GOLD = "transfers/gold"
        const val CRYPTO = "transfers/crypto"
        const val TRANSFER_HISTORY = "transfers/history"

        // Transactions
        const val TRANSACTIONS = "transactions"
        fun transaction(id: Int) = "transactions/$id"
        fun transactionReceipt(id: Int) = "transactions/$id/receipt"

        // Forex
        const val FOREX_RATES = "forex/rates"
        const val FOREX_CONVERT = "forex/convert"

        // Dashboard
        const val DASHBOARD = "dashboard"
    }

    // Shared Preferences Keys
    object PrefsKeys {
        const val AUTH_TOKEN = "auth_token"
        const val USER_ID = "user_id"
        const val USER_EMAIL = "user_email"
        const val USER_NAME = "user_name"
        const val DEVICE_TOKEN = "device_token"
        const val BIOMETRIC_ENABLED = "biometric_enabled"
        const val LAST_SYNC = "last_sync"
        const val IS_FIRST_LAUNCH = "is_first_launch"
        const val SELECTED_CURRENCY = "selected_currency"
    }

    // Intent Keys
    object IntentKeys {
        const val TRANSACTION_ID = "transaction_id"
        const val WALLET_CURRENCY = "wallet_currency"
        const val TRANSFER_TYPE = "transfer_type"
        const val RECIPIENT_EMAIL = "recipient_email"
        const val AMOUNT = "amount"
    }

    // Transaction Types
    object TransactionTypes {
        const val DEPOSIT = "deposit"
        const val WITHDRAWAL = "withdrawal"
        const val TRANSFER = "transfer"
        const val INSURANCE = "insurance"
        const val INVESTMENT = "investment"
        const val BILL_PAYMENT = "bill_payment"
        const val GOLD_PURCHASE = "gold_purchase"
        const val CRYPTO_PURCHASE = "crypto_purchase"
    }

    // Transaction Status
    object TransactionStatus {
        const val PENDING = "pending"
        const val SUCCESS = "success"
        const val FAILED = "failed"
        const val CANCELLED = "cancelled"
    }

    // Currencies
    object Currencies {
        val SUPPORTED = listOf("UGX", "USD", "EUR", "GBP")
        const val DEFAULT = "UGX"
    }

    // Transaction Limits
    object Limits {
        const val MIN_TRANSACTION_AMOUNT = 1000.0
        const val MAX_TRANSACTION_AMOUNT = 5_000_000.0
        const val MIN_WITHDRAWAL_AMOUNT = 5000.0
        const val MIN_DEPOSIT_AMOUNT = 1000.0
    }

    // Validation Rules
    object Validation {
        const val MIN_PASSWORD_LENGTH = 8
        const val PHONE_NUMBER_PREFIX = "256"
        const val PHONE_NUMBER_LENGTH = 12 // Including prefix
        const val OTP_LENGTH = 6
        val EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    }

    // Payment Providers
    object PaymentProviders {
        const val MTN = "mtn"
        const val AIRTEL = "airtel"
        const val FLUTTERWAVE = "flutterwave"
    }

    // HTTP Status Codes
    object StatusCodes {
        const val SUCCESS = 200
        const val CREATED = 201
        const val NO_CONTENT = 204
        const val BAD_REQUEST = 400
        const val UNAUTHORIZED = 401
        const val FORBIDDEN = 403
        const val NOT_FOUND = 404
        const val VALIDATION_ERROR = 422
        const val TOO_MANY_REQUESTS = 429
        const val SERVER_ERROR = 500
    }

    // Date Formats
    object DateFormats {
        const val API_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        const val DISPLAY_DATE = "dd MMM yyyy"
        const val DISPLAY_TIME = "HH:mm"
        const val DISPLAY_DATETIME = "dd MMM yyyy, HH:mm"
    }

    // Notification Channels
    object NotificationChannels {
        const val TRANSACTION_CHANNEL_ID = "transaction_notifications"
        const val TRANSACTION_CHANNEL_NAME = "Transaction Updates"
        const val GENERAL_CHANNEL_ID = "general_notifications"
        const val GENERAL_CHANNEL_NAME = "General Notifications"
    }

    // Request Codes
    object RequestCodes {
        const val CAMERA_PERMISSION = 100
        const val GALLERY_PERMISSION = 101
        const val BIOMETRIC_AUTH = 102
        const val PICK_IMAGE = 103
    }

    // Error Messages
    object ErrorMessages {
        const val NO_INTERNET = "No internet connection. Please check your network."
        const val SERVER_ERROR = "Server error. Please try again later."
        const val UNKNOWN_ERROR = "An unexpected error occurred."
        const val SESSION_EXPIRED = "Your session has expired. Please login again."
        const val INSUFFICIENT_BALANCE = "Insufficient balance for this transaction."
    }
}