package com.example.rojgar.repository

import android.util.Log
import com.example.rojgar.model.ApplicationModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class ApplicationRepoImpl : ApplicationRepo {

    private val database = FirebaseDatabase.getInstance()
    private val applicationsRef = database.getReference("Applications")

    override fun applyForJob(
        application: ApplicationModel,
        callback: (Boolean, String) -> Unit
    ) {
        // Generate unique application ID if not provided
        val applicationId = if (application.applicationId.isEmpty()) {
            UUID.randomUUID().toString()
        } else {
            application.applicationId
        }

        val applicationWithId = application.copy(applicationId = applicationId)

        applicationsRef.child(applicationId)
            .setValue(applicationWithId)
            .addOnSuccessListener {
                callback(true, "Application submitted successfully!")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to submit application: ${e.message}")
            }
    }

    override fun getApplicationsByJobSeeker(
        jobSeekerId: String,
        callback: (Boolean, String, List<ApplicationModel>?) -> Unit
    ) {
        Log.d("ApplicationRepo", "Fetching applications for jobSeekerId: $jobSeekerId")

        applicationsRef.orderByChild("jobSeekerId")
            .equalTo(jobSeekerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val applications = mutableListOf<ApplicationModel>()

                    Log.d("ApplicationRepo", "Snapshot exists: ${snapshot.exists()}")
                    Log.d("ApplicationRepo", "Children count: ${snapshot.childrenCount}")

                    for (data in snapshot.children) {
                        val application = data.getValue(ApplicationModel::class.java)
                        if (application != null) {
                            applications.add(application)
                            Log.d("ApplicationRepo", "Application: id=${application.applicationId}, status=${application.status}, feedback=${application.rejectionFeedback}")
                        }
                    }

                    Log.d("ApplicationRepo", "Total applications fetched: ${applications.size}")
                    callback(true, "Applications fetched successfully", applications)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ApplicationRepo", "Error fetching applications: ${error.message}")
                    callback(false, error.message, null)
                }
            })
    }

    override fun getApplicationsByCompany(
        companyId: String,
        callback: (Boolean, String, List<ApplicationModel>?) -> Unit
    ) {
        applicationsRef.orderByChild("companyId")
            .equalTo(companyId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val applications = mutableListOf<ApplicationModel>()

                    for (data in snapshot.children) {
                        val application = data.getValue(ApplicationModel::class.java)
                        application?.let { applications.add(it) }
                    }

                    callback(true, "Applications fetched successfully", applications)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun updateApplicationStatus(
        applicationId: String,
        status: String,
        rejectionFeedback: String?,
        callback: (Boolean, String) -> Unit
    ) {
        Log.d("ApplicationRepo", "Updating application: $applicationId")
        Log.d("ApplicationRepo", "New status: $status")
        Log.d("ApplicationRepo", "Feedback: $rejectionFeedback")

        val updates = hashMapOf<String, Any>(
            "status" to status
        )

        // Add rejection feedback if status is Rejected and feedback is provided
        if (status == "Rejected" && !rejectionFeedback.isNullOrEmpty()) {
            updates["rejectionFeedback"] = rejectionFeedback
            updates["rejectionDate"] = System.currentTimeMillis()
            Log.d("ApplicationRepo", "Adding rejection feedback and date")
        }

        Log.d("ApplicationRepo", "Updates to apply: $updates")

        applicationsRef.child(applicationId)
            .updateChildren(updates)
            .addOnSuccessListener {
                Log.d("ApplicationRepo", "Update successful")
                callback(true, "Application status updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("ApplicationRepo", "Update failed: ${e.message}")
                callback(false, "Failed to update status: ${e.message}")
            }
    }

    override fun deleteApplication(
        applicationId: String,
        callback: (Boolean, String) -> Unit
    ) {
        applicationsRef.child(applicationId)
            .removeValue()
            .addOnSuccessListener {
                callback(true, "Application deleted successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to delete application: ${e.message}")
            }
    }

    override fun checkIfApplied(
        jobSeekerId: String,
        postId: String,
        callback: (Boolean) -> Unit
    ) {
        Log.d("ApplicationRepo", "Checking if applied: jobSeekerId=$jobSeekerId, postId=$postId")

        applicationsRef.orderByChild("jobSeekerId")
            .equalTo(jobSeekerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val hasApplied = snapshot.children.any {
                        val applicationPostId = it.child("postId").getValue(String::class.java)
                        Log.d("ApplicationRepo", "Checking application: postId=$applicationPostId")
                        applicationPostId == postId
                    }
                    Log.d("ApplicationRepo", "Has applied: $hasApplied")
                    callback(hasApplied)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ApplicationRepo", "Error checking application: ${error.message}")
                    callback(false)
                }
            }
        )
    }
}