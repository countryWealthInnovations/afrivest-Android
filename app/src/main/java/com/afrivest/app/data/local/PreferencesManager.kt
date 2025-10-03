package com.afrivest.app.data.local

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
}