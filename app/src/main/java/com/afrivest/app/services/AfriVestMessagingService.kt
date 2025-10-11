package com.afrivest.app.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.afrivest.app.R
import com.afrivest.app.data.local.SecurePreferences
import com.afrivest.app.ui.main.MainActivity
import com.afrivest.app.ui.transactions.TransactionDetailActivity
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

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Timber.d("Message received from: ${message.from}")

        // Handle notification payload
        message.notification?.let { notification ->
            val title = notification.title ?: "AfriVest"
            val body = notification.body ?: ""

            // Extract data
            val transactionId = message.data["transaction_id"]?.toIntOrNull()
            val reference = message.data["reference"]
            val type = message.data["type"]
            val status = message.data["status"]

            showNotification(title, body, transactionId, reference, type, status)
        }

        // Handle data-only messages
        if (message.data.isNotEmpty()) {
            val title = when (message.data["type"]) {
                "deposit" -> "Deposit ${message.data["status"]?.capitalize()}"
                "withdrawal" -> "Withdrawal ${message.data["status"]?.capitalize()}"
                "transfer" -> "Transfer ${message.data["status"]?.capitalize()}"
                else -> "Transaction Update"
            }

            val body = message.data["reference"] ?: "Transaction completed"
            val transactionId = message.data["transaction_id"]?.toIntOrNull()

            showNotification(title, body, transactionId, message.data["reference"],
                message.data["type"], message.data["status"])
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(
        title: String,
        body: String,
        transactionId: Int?,
        reference: String?,
        type: String?,
        status: String?
    ) {
        val channelId = "afrivest_transactions"

        // Create notification channel (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Transactions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Transaction notifications"
                enableVibration(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open transaction detail
        val intent = if (transactionId != null) {
            Intent(this, TransactionDetailActivity::class.java).apply {
                putExtra("TRANSACTION_ID", transactionId)
                putExtra("REFERENCE", reference)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        } else {
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            transactionId ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setColor(getColor(R.color.primary_gold))
            .build()

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(transactionId ?: System.currentTimeMillis().toInt(), notification)
    }
}