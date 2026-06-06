package co.afrivest.data.model

data class NotificationSettingsData(
    val push_enabled: Boolean,
    val email_enabled: Boolean,
    val sms_enabled: Boolean
)