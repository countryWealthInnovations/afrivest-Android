package com.afrivest.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.afrivest.app.utils.Constants
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class AfriVestApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        Timber.plant(Timber.DebugTree())

        // Create notification channels
        createNotificationChannels()
        getFCMToken()

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