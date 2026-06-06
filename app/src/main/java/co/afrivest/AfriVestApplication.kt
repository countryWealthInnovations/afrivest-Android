package co.afrivest

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import co.afrivest.data.api.ApiService
import co.afrivest.data.local.PreferencesManager
import co.afrivest.utils.Constants
import co.afrivest.utils.CrashlyticsTree
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import co.afrivest.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class AfriVestApplication : Application() {

    companion object {
        lateinit var instance: AfriVestApplication
            private set
    }

    override fun onCreate() {
        instance = this
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }

        // Create notification channels
        createNotificationChannels()
        getFCMToken()
        refreshForexRates()

        Timber.d("AfriVest Application Started")
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Transaction notifications channel
            val transactionChannel = NotificationChannel(
                Constants.NotificationChannels.TRANSACTION_CHANNEL_ID,
                Constants.NotificationChannels.TRANSACTION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for transaction updates"
                enableVibration(true)
                enableLights(true)
            }

            // General notifications channel
            val generalChannel = NotificationChannel(
                Constants.NotificationChannels.GENERAL_CHANNEL_ID,
                Constants.NotificationChannels.GENERAL_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }

            notificationManager.createNotificationChannel(transactionChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }

    @Inject lateinit var apiService: ApiService
    @Inject lateinit var preferencesManager: PreferencesManager

    private fun refreshForexRates() {
        val token = preferencesManager.authToken ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getForexRates()
                if (response.isSuccessful) {
                    val forexData = response.body()?.data ?: return@launch
                    val newRates = forexData.rates

                    val stored = preferencesManager.forexRates
                    if (newRates != stored) {
                        preferencesManager.forexRates = newRates
                        Timber.d("✅ Forex rates updated: $newRates")
                    } else {
                        Timber.d("ℹ️ Forex rates unchanged")
                    }
                }
            } catch (e: Exception) {
                Timber.w("⚠️ Forex rate refresh failed: ${e.message}")
            }
        }
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Timber.d("FCM Token: $token")
            } else {
                Timber.e(task.exception, "Failed to get FCM token")
            }
        }
    }
}