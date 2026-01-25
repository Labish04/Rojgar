package com.example.rojgar.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.rojgar.R
import com.example.rojgar.view.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send this token to your server if you need to target this specific device
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Show notification if message contains a notification payload
        remoteMessage.notification?.let {
            showNotification(it.title, it.body)
        }
    }

    // Inside MyFirebaseMessagingService or another Context-aware class
    private fun showNotification(title: String?, message: String?) {
        val channelId = "rojgar_notifications"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a Notification Channel (Required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Job Alerts", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // Set what happens when the user taps the notification
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        // Build the notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ensure this icon exists
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Show it
        notificationManager.notify(0, notification)
    }
}