package com.example.rojgar.repository

import com.example.rojgar.model.SavedJobModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class SavedJobRepoImpl : SavedJobRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("SavedJobs")

    override fun saveJob(
        savedJob: SavedJobModel,
        callback: (Boolean, String) -> Unit
    ) {
        val savedId = ref.push().key ?: UUID.randomUUID().toString()
        val savedJobWithId = savedJob.copy(savedId = savedId)

        ref.child(savedId).setValue(savedJobWithId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Job saved successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to save job")
                }
            }
    }

    override fun unsaveJob(
        savedId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(savedId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Job unsaved successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to unsave job")
                }
            }
    }

    override fun getSavedJobsByJobSeeker(
        jobSeekerId: String,
        callback: (Boolean, String, List<SavedJobModel>?) -> Unit
    ) {
        ref.orderByChild("jobSeekerId").equalTo(jobSeekerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val savedJobs = mutableListOf<SavedJobModel>()

                    if (!snapshot.exists()) {
                        callback(true, "No saved jobs found", emptyList())
                        return
                    }

                    for (child in snapshot.children) {
                        val savedJob = child.getValue(SavedJobModel::class.java)
                        if (savedJob != null) {
                            savedJobs.add(savedJob.copy(savedId = child.key ?: savedJob.savedId))
                        }
                    }

                    // Sort by savedAt (newest first)
                    savedJobs.sortByDescending { it.savedAt }

                    callback(true, "Fetched ${savedJobs.size} saved jobs", savedJobs)
                }

                override fun onCancelled(error: DatabaseError
                ) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun checkIfJobSaved(
        jobSeekerId: String,
        jobId: String,
        callback: (Boolean, String, Boolean, String?) -> Unit
    ) {
        ref.orderByChild("jobSeekerId").equalTo(jobSeekerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        callback(true, "Job not saved", false, null)
                        return
                    }

                    for (child in snapshot.children) {
                        val savedJob = child.getValue(SavedJobModel::class.java)
                        if (savedJob != null && savedJob.jobId == jobId) {
                            callback(true, "Job is saved", true, child.key)
                            return
                        }
                    }

                    callback(true, "Job not saved", false, null)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, false, null)
                }
            })
    }

    override fun getSavedJobDetails(
        jobSeekerId: String,
        jobId: String,
        callback: (Boolean, String, SavedJobModel?) -> Unit
    ) {
        ref.orderByChild("jobSeekerId").equalTo(jobSeekerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        callback(false, "No saved jobs found", null)
                        return
                    }

                    for (child in snapshot.children) {
                        val savedJob = child.getValue(SavedJobModel::class.java)
                        if (savedJob != null && savedJob.jobId == jobId) {
                            callback(true, "Saved job found", savedJob.copy(savedId = child.key ?: savedJob.savedId))
                            return
                        }
                    }

                    callback(false, "Job not saved", null)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }
}