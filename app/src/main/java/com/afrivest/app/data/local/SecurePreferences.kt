package com.afrivest.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.afrivest.app.utils.Constants

class SecurePreferences(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "afrivest_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val regularPrefs: SharedPreferences = context.getSharedPreferences(
        "afrivest_prefs",
        Context.MODE_PRIVATE
    )

    // ==================== AUTH TOKEN ====================

    fun saveAuthToken(token: String) {
        encryptedPrefs.edit().putString(Constants.PrefsKeys.AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return encryptedPrefs.getString(Constants.PrefsKeys.AUTH_TOKEN, null)
    }

    fun clearAuthToken() {
        encryptedPrefs.edit().remove(Constants.PrefsKeys.AUTH_TOKEN).apply()
    }

    fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }

    // ==================== USER DATA ====================

    fun saveUserId(userId: Int) {
        regularPrefs.edit().putInt(Constants.PrefsKeys.USER_ID, userId).apply()
    }

    fun getUserId(): Int {
        return regularPrefs.getInt(Constants.PrefsKeys.USER_ID, -1)
    }

    fun saveUserEmail(email: String) {
        regularPrefs.edit().putString(Constants.PrefsKeys.USER_EMAIL, email).apply()
    }

    fun getUserEmail(): String? {
        return regularPrefs.getString(Constants.PrefsKeys.USER_EMAIL, null)
    }

    fun saveUserName(name: String) {
        regularPrefs.edit().putString(Constants.PrefsKeys.USER_NAME, name).apply()
    }

    fun getUserName(): String? {
        return regularPrefs.getString(Constants.PrefsKeys.USER_NAME, null)
    }

    // ==================== DEVICE TOKEN ====================

    fun saveDeviceToken(token: String) {
        regularPrefs.edit().putString(Constants.PrefsKeys.DEVICE_TOKEN, token).apply()
    }

    fun getDeviceToken(): String? {
        return regularPrefs.getString(Constants.PrefsKeys.DEVICE_TOKEN, null)
    }

    // ==================== BIOMETRIC ====================

    fun setBiometricEnabled(enabled: Boolean) {
        regularPrefs.edit().putBoolean(Constants.PrefsKeys.BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isBiometricEnabled(): Boolean {
        return regularPrefs.getBoolean(Constants.PrefsKeys.BIOMETRIC_ENABLED, false)
    }

    // ==================== APP STATE ====================

    fun setFirstLaunch(isFirst: Boolean) {
        regularPrefs.edit().putBoolean(Constants.PrefsKeys.IS_FIRST_LAUNCH, isFirst).apply()
    }

    fun isFirstLaunch(): Boolean {
        return regularPrefs.getBoolean(Constants.PrefsKeys.IS_FIRST_LAUNCH, true)
    }

    fun saveSelectedCurrency(currency: String) {
        regularPrefs.edit().putString(Constants.PrefsKeys.SELECTED_CURRENCY, currency).apply()
    }

    fun getSelectedCurrency(): String {
        return regularPrefs.getString(
            Constants.PrefsKeys.SELECTED_CURRENCY,
            Constants.Currencies.DEFAULT
        ) ?: Constants.Currencies.DEFAULT
    }

    fun saveLastSync(timestamp: Long) {
        regularPrefs.edit().putLong(Constants.PrefsKeys.LAST_SYNC, timestamp).apply()
    }

    fun getLastSync(): Long {
        return regularPrefs.getLong(Constants.PrefsKeys.LAST_SYNC, 0)
    }

    // ==================== VERIFICATION STATUS ====================

    fun setEmailVerified(verified: Boolean) {
        regularPrefs.edit().putBoolean("email_verified", verified).apply()
    }

    fun isEmailVerified(): Boolean {
        return regularPrefs.getBoolean("email_verified", false)
    }

    fun setKYCVerified(verified: Boolean) {
        regularPrefs.edit().putBoolean("kyc_verified", verified).apply()
    }

    fun isKYCVerified(): Boolean {
        return regularPrefs.getBoolean("kyc_verified", false)
    }

    // ==================== CLEAR ALL ====================

    fun clearAll() {
        encryptedPrefs.edit().clear().apply()
        regularPrefs.edit().clear().apply()
    }
}

// ==================== PREFERENCES MANAGER (Non-Secure) ====================

