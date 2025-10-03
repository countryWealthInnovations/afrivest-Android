package com.afrivest.app.services

import com.afrivest.app.data.local.SecurePreferences
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AfriVestMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var securePreferences: SecurePreferences

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("New FCM token: $token")

        // Save token locally
        securePreferences.saveDeviceToken(token)

        // TODO: Send token to server if user is logged in
        if (securePreferences.isLoggedIn()) {
            // Call API to update device token
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Timber.d("Message received from: ${message.from}")

        // Handle notification payload
        message.notification?.let {
            val title = it.title
            val body = it.body
            // Show notification
        }

        // Handle data payload
        message.data.isNotEmpty().let {
            // Process data
        }
    }
}