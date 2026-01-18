package com.example.rojgar.repository

import com.example.rojgar.model.HelpAndSupportModel
import com.example.rojgar.model.RequestStatus
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class HelpAndSupportRepoImpl : HelpAndSupportRepo {

    private val database = FirebaseDatabase.getInstance()
    private val supportRequestsRef: DatabaseReference = database.getReference("SupportRequests")

    override suspend fun submitSupportRequest(request: HelpAndSupportModel): Result<String> {
        return suspendCoroutine { continuation ->
            val requestId = supportRequestsRef.push().key
                ?: "REQ${System.currentTimeMillis()}"

            val requestWithId = request.copy(id = requestId)

            supportRequestsRef.child(requestId).setValue(requestWithId)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(Result.success(requestId))
                    } else {
                        continuation.resume(
                            Result.failure(
                                task.exception ?: Exception("Failed to submit support request")
                            )
                        )
                    }
                }
        }
    }

    override suspend fun getSupportRequests(companyEmail: String): Flow<List<HelpAndSupportModel>> {
        return callbackFlow {
            val listener = supportRequestsRef
                .orderByChild("companyEmail")
                .equalTo(companyEmail)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val requestList = mutableListOf<HelpAndSupportModel>()

                        if (snapshot.exists()) {
                            for (requestSnapshot in snapshot.children) {
                                val request = requestSnapshot.getValue(HelpAndSupportModel::class.java)
                                request?.let {
                                    requestList.add(it)
                                }
                            }
                            // Sort by timestamp descending (newest first)
                            requestList.sortByDescending { it.timestamp }
                        }

                        trySend(requestList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        close(Exception(error.message))
                    }
                })

            awaitClose {
                supportRequestsRef.removeEventListener(listener)
            }
        }
    }

    override suspend fun updateRequestStatus(requestId: String, status: String): Result<Boolean> {
        return suspendCoroutine { continuation ->
            // Parse status string to enum
            val newStatus = try {
                RequestStatus.valueOf(status.uppercase().replace(" ", "_"))
            } catch (e: IllegalArgumentException) {
                continuation.resume(Result.failure(Exception("Invalid status: $status")))
                return@suspendCoroutine
            }

            val updates = hashMapOf<String, Any>(
                "status" to newStatus.name
            )

            supportRequestsRef.child(requestId).updateChildren(updates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(Result.success(true))
                    } else {
                        continuation.resume(
                            Result.failure(
                                task.exception ?: Exception("Failed to update status")
                            )
                        )
                    }
                }
        }
    }

    override suspend fun deleteSupportRequest(requestId: String): Result<Boolean> {
        return suspendCoroutine { continuation ->
            supportRequestsRef.child(requestId).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(Result.success(true))
                    } else {
                        continuation.resume(
                            Result.failure(
                                task.exception ?: Exception("Failed to delete request")
                            )
                        )
                    }
                }
        }
    }

    // Additional utility methods

    fun getSupportRequestById(
        requestId: String,
        callback: (Boolean, String, HelpAndSupportModel?) -> Unit
    ) {
        supportRequestsRef.child(requestId).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val request = snapshot.getValue(HelpAndSupportModel::class.java)
                        callback(true, "Request fetched successfully", request)
                    } else {
                        callback(false, "Request not found", null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            }
        )
    }

    fun getAllSupportRequests(
        callback: (Boolean, String, List<HelpAndSupportModel>?) -> Unit
    ) {
        supportRequestsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requestList = mutableListOf<HelpAndSupportModel>()

                if (snapshot.exists()) {
                    for (requestSnapshot in snapshot.children) {
                        val request = requestSnapshot.getValue(HelpAndSupportModel::class.java)
                        request?.let {
                            requestList.add(it)
                        }
                    }
                    requestList.sortByDescending { it.timestamp }
                    callback(true, "All requests fetched", requestList)
                } else {
                    callback(true, "No requests found", emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    fun getSupportRequestsByStatus(
        status: RequestStatus,
        callback: (Boolean, String, List<HelpAndSupportModel>?) -> Unit
    ) {
        supportRequestsRef.orderByChild("status")
            .equalTo(status.name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val requestList = mutableListOf<HelpAndSupportModel>()

                    if (snapshot.exists()) {
                        for (requestSnapshot in snapshot.children) {
                            val request = requestSnapshot.getValue(HelpAndSupportModel::class.java)
                            request?.let {
                                requestList.add(it)
                            }
                        }
                        requestList.sortByDescending { it.timestamp }
                        callback(true, "Requests fetched by status", requestList)
                    } else {
                        callback(true, "No requests found with this status", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    fun getSupportRequestsByCategory(
        category: String,
        callback: (Boolean, String, List<HelpAndSupportModel>?) -> Unit
    ) {
        supportRequestsRef.orderByChild("category")
            .equalTo(category)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val requestList = mutableListOf<HelpAndSupportModel>()

                    if (snapshot.exists()) {
                        for (requestSnapshot in snapshot.children) {
                            val request = requestSnapshot.getValue(HelpAndSupportModel::class.java)
                            request?.let {
                                requestList.add(it)
                            }
                        }
                        requestList.sortByDescending { it.timestamp }
                        callback(true, "Requests fetched by category", requestList)
                    } else {
                        callback(true, "No requests found in this category", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    fun updateSupportRequest(
        requestId: String,
        updatedRequest: HelpAndSupportModel,
        callback: (Boolean, String) -> Unit
    ) {
        supportRequestsRef.child(requestId).updateChildren(updatedRequest.toMap())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Request updated successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to update request")
                }
            }
    }
}

// Extension function to convert HelpAndSupportModel to Map
fun HelpAndSupportModel.toMap(): Map<String, Any> {
    return mapOf(
        "id" to id,
        "companyName" to companyName,
        "companyEmail" to companyEmail,
        "subject" to subject,
        "category" to category.name,
        "priority" to priority.name,
        "description" to description,
        "timestamp" to timestamp,
        "status" to status.name,
        "attachments" to attachments
    )
}