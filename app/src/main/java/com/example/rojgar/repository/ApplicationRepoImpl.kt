package com.example.rojgar.repository

import android.content.Context
import android.util.Log
import com.example.rojgar.model.ApplicationModel
import com.example.rojgar.utils.NotificationHelper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class ApplicationRepoImpl(private val context: Context) : ApplicationRepo {

    private val database = FirebaseDatabase.getInstance()
    private val applicationsRef = database.getReference("Applications")
    private val jobsRef = database.getReference("Jobs")

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
                sendApplicationNotificationToCompany(context, applicationWithId)
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

        // FIRST, fetch the complete application data
        applicationsRef.child(applicationId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val application = snapshot.getValue(ApplicationModel::class.java)

                    if (application == null) {
                        Log.e("ApplicationRepo", "Application not found: $applicationId")
                        callback(false, "Application not found")
                        return
                    }

                    // Now update the status
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
                            // Send notification with the complete application data
                            sendStatusUpdateNotificationToJobSeeker(
                                context,
                                application, // Pass the complete application object
                                status,
                                rejectionFeedback
                            )
                            Log.d("ApplicationRepo", "Update successful")
                            callback(true, "Application status updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("ApplicationRepo", "Update failed: ${e.message}")
                            callback(false, "Failed to update status: ${e.message}")
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ApplicationRepo", "Error fetching application: ${error.message}")
                    callback(false, "Failed to fetch application: ${error.message}")
                }
            })
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

    private fun sendApplicationNotificationToCompany(
        context: Context,
        application: ApplicationModel
    ) {
        // Get job seeker details
        database.getReference("JobSeekers").child(application.jobSeekerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val jobSeekerName = snapshot.child("fullName").getValue(String::class.java) ?: "A job seeker"

                    // Get job details for the notification
                    jobsRef.child(application.postId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(jobSnapshot: DataSnapshot) {
                                val jobTitle = jobSnapshot.child("title").getValue(String::class.java) ?: "a job"

                                // Send notification to company
                                NotificationHelper.sendJobApplicationNotification(
                                    context = context,
                                    companyId = application.companyId,
                                    jobSeekerName = jobSeekerName,
                                    jobTitle = jobTitle,
                                    applicationId = application.applicationId,
                                    jobId = application.postId
                                )
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("ApplicationRepo", "Error getting job details: ${error.message}")
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ApplicationRepo", "Error getting job seeker details: ${error.message}")
                }
            })
    }

    // Helper function to send notification to job seeker when status is updated
    private fun sendStatusUpdateNotificationToJobSeeker(
        context: Context,
        application: ApplicationModel,
        status: String,
        feedback: String?
    ) {
        // Get company details
        database.getReference("Companys").child(application.companyId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val companyName = snapshot.child("companyName").getValue(String::class.java) ?: "A company"

                    // Get job details
                    jobsRef.child(application.postId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(jobSnapshot: DataSnapshot) {
                                val jobTitle = jobSnapshot.child("title").getValue(String::class.java) ?: "a job"

                                // Send notification to job seeker
                                NotificationHelper.sendApplicationStatusNotification(
                                    context = context,
                                    jobSeekerId = application.jobSeekerId,
                                    companyName = companyName,
                                    jobTitle = jobTitle,
                                    status = status,
                                    message = "Your application status has been updated to $status",
                                    feedback = feedback,
                                    applicationId = application.applicationId,
                                    jobId = application.postId
                                )
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("ApplicationRepo", "Error getting job details: ${error.message}")
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ApplicationRepo", "Error getting company details: ${error.message}")
                }
            })
    }
}