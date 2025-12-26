package com.example.rojgar.repository

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
        applicationsRef.orderByChild("jobSeekerId")
            .equalTo(jobSeekerId)
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
        callback: (Boolean, String) -> Unit
    ) {
        applicationsRef.child(applicationId).child("status")
            .setValue(status)
            .addOnSuccessListener {
                callback(true, "Application status updated successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to update status: ${e.message}")
            }
    }
}