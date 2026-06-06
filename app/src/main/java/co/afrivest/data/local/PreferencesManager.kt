package co.afrivest.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "afrivest_settings",
        Context.MODE_PRIVATE
    )

    // ==================== SETTINGS ====================

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun isNotificationsEnabled(): Boolean {
        return prefs.getBoolean("notifications_enabled", true)
    }

    fun setTransactionNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("transaction_notifications_enabled", enabled).apply()
    }

    fun isTransactionNotificationsEnabled(): Boolean {
        return prefs.getBoolean("transaction_notifications_enabled", true)
    }

    fun setMarketingNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("marketing_notifications_enabled", enabled).apply()
    }

    fun isMarketingNotificationsEnabled(): Boolean {
        return prefs.getBoolean("marketing_notifications_enabled", false)
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode_enabled", enabled).apply()
    }

    fun isDarkModeEnabled(): Boolean {
        return prefs.getBoolean("dark_mode_enabled", false)
    }

    fun setLanguage(language: String) {
        prefs.edit().putString("language", language).apply()
    }

    fun getLanguage(): String {
        return prefs.getString("language", "en") ?: "en"
    }

    // ==================== ONBOARDING ====================

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_completed", completed).apply()
    }

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean("onboarding_completed", false)
    }
    var defaultCurrency: String?
        get() = prefs.getString("default_currency", null)
        set(v) = prefs.edit().putString("default_currency", v).apply()

    var secondaryCurrency: String?
        get() = prefs.getString("secondary_currency", null)
        set(value) = prefs.edit().putString("secondary_currency", value).apply()

    var kycBannerHidden: Boolean
        get() = prefs.getBoolean("kyc_banner_hidden", false)
        set(value) = prefs.edit().putBoolean("kyc_banner_hidden", value).apply()

    var notifPush: Boolean
        get() = prefs.getBoolean("notif_push", true)
        set(value) = prefs.edit().putBoolean("notif_push", value).apply()

    var notifEmail: Boolean
        get() = prefs.getBoolean("notif_email", true)
        set(value) = prefs.edit().putBoolean("notif_email", value).apply()

    var notifSms: Boolean
        get() = prefs.getBoolean("notif_sms", false)
        set(value) = prefs.edit().putBoolean("notif_sms", value).apply()

    var authToken: String?
        get() = prefs.getString("auth_token", null)
        set(value) = prefs.edit().putString("auth_token", value).apply()

    var forexRates: Map<String, Double>
        get() {
            val json = prefs.getString("forex_rates", null) ?: return emptyMap()
            return try {
                val type = object : com.google.gson.reflect.TypeToken<Map<String, Double>>() {}.type
                com.google.gson.Gson().fromJson(json, type)
            } catch (e: Exception) { emptyMap() }
        }
        set(value) {
            val json = com.google.gson.Gson().toJson(value)
            prefs.edit().putString("forex_rates", json).apply()
        }
}