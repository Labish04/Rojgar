package com.example.rojgar.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.RemoteInput
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MessageBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "REPLY_ACTION" -> {
                handleReply(context, intent)
            }
            "MARK_AS_READ_ACTION" -> {
                handleMarkAsRead(intent)
            }
        }
    }

    private fun handleReply(context: Context, intent: Intent) {
        val senderId = intent.getStringExtra("senderId") ?: return
        val messageId = intent.getStringExtra("messageId") ?: return

        // Get reply text from RemoteInput
        val results = RemoteInput.getResultsFromIntent(intent)
        val replyText = results?.getCharSequence("reply_text")?.toString()

        if (replyText != null && replyText.isNotBlank()) {
            // Send quick reply
            sendQuickReply(senderId, replyText)

            // Show confirmation
            NotificationHelper.showNotification(
                context,
                "Reply Sent",
                "Your reply has been sent",
                NotificationHelper.TYPE_GENERAL
            )
        }
    }

    private fun sendQuickReply(receiverId: String, message: String) {
        // Implement quick reply logic here
        // This would typically save the message to your database
        val messageData = hashMapOf<String, Any>(
            ("senderId" to FirebaseAuth.getInstance().currentUser?.uid ?: "") as Pair<String, Any>,
            "receiverId" to receiverId,
            "message" to message,
            "timestamp" to System.currentTimeMillis(),
            "isQuickReply" to true
        )

        FirebaseDatabase.getInstance()
            .getReference("Messages")
            .push()
            .setValue(messageData)
    }

    private fun handleMarkAsRead(intent: Intent) {
        val messageId = intent.getStringExtra("messageId") ?: return

        // Mark notification as read in database
        FirebaseDatabase.getInstance()
            .getReference("MessageNotifications")
            .child(FirebaseAuth.getInstance().currentUser?.uid ?: "")
            .child(messageId)
            .child("isRead")
            .setValue(true)
    }
}