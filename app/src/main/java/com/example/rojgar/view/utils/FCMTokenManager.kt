package com.example.rojgar.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

object FCMTokenManager {

    private const val TAG = "FCMTokenManager"

    fun registerFCMToken(userId: String, userType: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d(TAG, "FCM Token: $token")

            // Save token to the appropriate user node
            saveFCMToken(userId, token, userType)
        }
    }

    private fun saveFCMToken(userId: String, token: String, userType: String) {
        val database = FirebaseDatabase.getInstance()

        // Save to the specific user type node
        val userRef = when (userType) {
            "JOBSEEKER" -> database.getReference("JobSeekers").child(userId)
            "COMPANY" -> database.getReference("Companys").child(userId)
            else -> return
        }
        val updates = hashMapOf<String, Any>(
            "fcmToken" to token
        )

        // Save token
        userRef.child("fcmToken").setValue(token)
            .addOnSuccessListener {
                Log.d(TAG, "FCM token saved successfully for $userType: $userId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save FCM token", e)
            }

        // Also save to a general tokens collection for easier notification targeting
        database.getReference("fcmTokens")
            .child(userId)
            .setValue(mapOf(
                "token" to token,
                "userType" to userType,
                "timestamp" to System.currentTimeMillis()
            ))
    }

    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }
}