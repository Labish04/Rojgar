// file name: HelpSupportRepoImpl.kt
package com.example.rojgar.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import androidx.compose.runtime.mutableStateListOf
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.rojgar.model.HelpSupportModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class HelpSupportRepoImpl : HelpSupportRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val helpRequestsRef: DatabaseReference = database.getReference("HelpSupportRequests")

    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dtmprduic",
            "api_key" to "883843915169633",
            "api_secret" to "DhiLcks25VLVZCBhWgGvObdGGyE"
        )
    )

    private fun generateRequestId(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val random = (1000..9999).random()
        return "HELP_${timestamp}_${random}"
    }

    private fun getCurrentDateTime(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }

    override fun submitHelpRequest(
        model: HelpSupportModel,
        callback: (Boolean, String) -> Unit
    ) {
        // Generate request ID if not provided
        val requestId = if (model.requestId.isEmpty()) {
            generateRequestId()
        } else {
            model.requestId
        }

        // Set timestamps
        val currentTime = getCurrentDateTime()
        val updatedModel = model.copy(
            requestId = requestId,
            createdAt = if (model.createdAt.isEmpty()) currentTime else model.createdAt,
            updatedAt = currentTime
        )

        // Save to help requests with requestId as key
        helpRequestsRef
            .child(requestId)
            .setValue(updatedModel.toMap())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Help request submitted successfully. Request ID: $requestId")
                } else {
                    callback(false, "Failed to submit request: ${task.exception?.message}")
                }
            }
    }

    override fun uploadScreenshot(
        context: Context,
        imageUri: Uri,
        userId: String,
        callback: (String?) -> Unit
    ) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                val fileName = getFileNameFromUri(context, imageUri)
                val timestamp = System.currentTimeMillis()

                val publicId = "help_screenshot_${userId}_${timestamp}"

                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", publicId,
                        "resource_type", "image"
                    )
                )

                var imageUrl = response["url"] as String?
                imageUrl = imageUrl?.replace("http://", "https://")

                Handler(Looper.getMainLooper()).post {
                    callback(imageUrl)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    callback(null)
                }
            }
        }
    }

    private fun getFileNameFromUri(
        context: Context,
        imageUri: Uri
    ): String? {
        var fileName: String? = null
        val cursor: Cursor? = context.contentResolver.query(imageUri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName ?: "screenshot_${System.currentTimeMillis()}"
    }

    override fun getUserHelpRequests(
        userId: String,
        callback: (Boolean, String, List<HelpSupportModel>?) -> Unit
    ) {
        // Query to get only the current user's requests
        helpRequestsRef
            .orderByChild("userId")
            .equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val requests = mutableStateListOf<HelpSupportModel>()
                        for (data in snapshot.children) {
                            val request = data.getValue(HelpSupportModel::class.java)
                            request?.let {
                                requests.add(it)
                            }
                        }
                        // Sort by creation date (newest first)
                        val sortedRequests = requests.sortedByDescending { it.createdAt }
                        callback(true, "Requests fetched successfully", sortedRequests)
                    } else {
                        callback(true, "No help requests found", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, emptyList())
                }
            })
    }

    override fun getHelpRequestById(
        requestId: String,
        callback: (Boolean, String, HelpSupportModel?) -> Unit
    ) {
        helpRequestsRef.child(requestId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val request = snapshot.getValue(HelpSupportModel::class.java)
                        callback(true, "Request found", request)
                    } else {
                        callback(false, "Request not found", null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getAllHelpRequests(
        callback: (Boolean, String, List<HelpSupportModel>?) -> Unit
    ) {
        helpRequestsRef
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val requests = mutableStateListOf<HelpSupportModel>()
                        for (data in snapshot.children) {
                            val request = data.getValue(HelpSupportModel::class.java)
                            request?.let {
                                requests.add(it)
                            }
                        }
                        // Sort by priority and creation date
                        val sortedRequests = requests.sortedWith(
                            compareByDescending<HelpSupportModel> {
                                when (it.priority) {
                                    "Urgent" -> 4
                                    "High" -> 3
                                    "Medium" -> 2
                                    "Low" -> 1
                                    else -> 0
                                }
                            }.thenByDescending { it.createdAt }
                        )
                        callback(true, "All requests fetched", sortedRequests)
                    } else {
                        callback(true, "No requests found", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, emptyList())
                }
            })
    }

    override fun updateRequestStatus(
        requestId: String,
        status: String,
        adminNotes: String,
        resolvedBy: String,
        callback: (Boolean, String) -> Unit
    ) {
        getHelpRequestById(requestId) { success, message, request ->
            if (success && request != null) {
                val updatedTime = getCurrentDateTime()
                val updatedModel = request.copy(
                    status = status,
                    adminNotes = adminNotes,
                    resolvedBy = resolvedBy,
                    resolvedAt = if (status == "Resolved" || status == "Closed") updatedTime else "",
                    updatedAt = updatedTime
                )

                // Update the request
                helpRequestsRef.child(requestId)
                    .setValue(updatedModel.toMap())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            callback(true, "Request status updated to $status")
                        } else {
                            callback(false, "Failed to update request: ${task.exception?.message}")
                        }
                    }
            } else {
                callback(false, "Request not found: $message")
            }
        }
    }
}