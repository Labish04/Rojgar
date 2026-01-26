package com.example.rojgar.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.rojgar.R
import com.example.rojgar.view.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")

        // Save token to Firebase Database for the current user
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Get user type and save token
            getUserTypeAndSaveToken(userId, token)
        } else {
            // Save token locally to register later when user logs in
            saveTokenLocally(token)
        }
    }

    private fun getUserTypeAndSaveToken(userId: String, token: String) {
        val database = FirebaseDatabase.getInstance()

        // Check if user is JobSeeker
        database.getReference("JobSeekers").child(userId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    FCMTokenManager.registerFCMToken(userId, "JOBSEEKER")
                } else {
                    // Check if user is Company
                    database.getReference("Companys").child(userId).get()
                        .addOnSuccessListener { companySnapshot ->
                            if (companySnapshot.exists()) {
                                FCMTokenManager.registerFCMToken(userId, "COMPANY")
                            }
                        }
                }
            }
    }

    private fun saveTokenLocally(token: String) {
        // Save to SharedPreferences for later registration
        val prefs = getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("pending_token", token).apply()
        Log.d("FCM", "Token saved locally for later registration")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "Message received from: ${remoteMessage.from}")

        // Handle notification payload
        remoteMessage.notification?.let {
            showNotification(it.title, it.body)
        }

        // Handle data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")
            val title = remoteMessage.data["title"]
            val message = remoteMessage.data["message"]
            val type = remoteMessage.data["type"]
            showNotification(title, message, type)
        }
    }

    private fun showNotification(title: String?, message: String?, type: String? = null) {
        val channelId = "rojgar_notifications"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Notification Channel (Required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Job Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for job alerts and updates"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent based on notification type
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("FROM_NOTIFICATION", true)
            putExtra("NOTIFICATION_TYPE", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title ?: "Rojgar")
            .setContentText(message ?: "You have a new notification")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        // Show notification with unique ID
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}