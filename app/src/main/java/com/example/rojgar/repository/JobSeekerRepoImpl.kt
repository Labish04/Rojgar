package com.example.rojgar.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.rojgar.model.AppliedJobModel
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.model.JobModel
import com.example.rojgar.model.JobSeekerModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class JobSeekerRepoImpl : JobSeekerRepo {

    val auth : FirebaseAuth = FirebaseAuth.getInstance()

    val database : FirebaseDatabase = FirebaseDatabase.getInstance()

    val ref : DatabaseReference = database.getReference("JobSeekers")

    val appliedJobsRef : DatabaseReference = database.getReference("AppliedJobs")

    override fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    callback(
                        true, "Registration Successful",
                        "${auth.currentUser?.uid}"
                    )
                }
                else {
                    callback(false, "${it.exception?.message}","")
                }
            }
    }

    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    callback(true, "Login Successful")
                }
                else {
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun addJobSeekerToDatabase(
        jobSeekerId: String,
        model: JobSeekerModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(jobSeekerId).setValue(model).addOnCompleteListener {
            if (it.isSuccessful){
                callback(true,"Registration Successful")
            }
            else{
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun getCurrentJobSeeker(): FirebaseUser? {
        return auth.currentUser
    }

    override fun getJobSeekerById(
        jobSeekerId: String,
        callback: (Boolean, String, JobSeekerModel?) -> Unit
    ) {
        ref.child(jobSeekerId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val jobSeeker = snapshot.getValue(JobSeekerModel::class.java)
                    if (jobSeeker != null) {
                        callback(true, "Profile fetched", jobSeeker)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }

        })
    }

    override fun getAllJobSeeker(callback: (Boolean, String, List<JobSeekerModel>?) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    var allJobSeekers = mutableStateListOf<JobSeekerModel>()
                    for (data in snapshot.children){
                        var jobSeeker = data.getValue(JobSeekerModel::class.java)
                        if (jobSeeker != null){
                            allJobSeekers.add(jobSeeker)
                        }
                    }
                    callback(true, "JobSeeker Fetched", allJobSeekers)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false,error.message,emptyList())
            }

        })
    }

    override fun logout(
        jobSeekerId: String,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            auth.signOut()
            callback(true, "Logout successfully")
        } catch (e: Exception) {
            callback(false, e.message.toString())
        }
    }

    override fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Link sent to $email")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun updateProfile(
        model: JobSeekerModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(model.jobSeekerId).updateChildren(model.toMap()).addOnCompleteListener {
            if(it.isSuccessful){
                callback(true,"Profile updated successfully")
            }else{
                callback(false,"${it.exception?.message}")
            }
        }
    }

    // ============================================
    // FOLLOW FUNCTIONALITY METHODS
    // ============================================

    override fun followJobSeeker(
        currentUserId: String,
        targetJobSeekerId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(targetJobSeekerId).child("followers")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentFollowers = mutableListOf<String>()

                    if (snapshot.exists()) {
                        for (followerSnapshot in snapshot.children) {
                            val followerId = followerSnapshot.getValue(String::class.java)
                            if (followerId != null) {
                                currentFollowers.add(followerId)
                            }
                        }
                    }

                    if (currentFollowers.contains(currentUserId)) {
                        callback(false, "Already following this user")
                        return
                    }

                    currentFollowers.add(currentUserId)

                    ref.child(targetJobSeekerId).child("followers").setValue(currentFollowers)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                callback(true, "Following successfully")
                            } else {
                                callback(false, task.exception?.message ?: "Failed to follow")
                            }
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message)
                }
            })
    }

    override fun unfollowJobSeeker(
        currentUserId: String,
        targetJobSeekerId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(targetJobSeekerId).child("followers")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentFollowers = mutableListOf<String>()

                    if (snapshot.exists()) {
                        for (followerSnapshot in snapshot.children) {
                            val followerId = followerSnapshot.getValue(String::class.java)
                            if (followerId != null) {
                                currentFollowers.add(followerId)
                            }
                        }
                    }

                    currentFollowers.remove(currentUserId)

                    ref.child(targetJobSeekerId).child("followers").setValue(currentFollowers)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                callback(true, "Unfollowed successfully")
                            } else {
                                callback(false, task.exception?.message ?: "Failed to unfollow")
                            }
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message)
                }
            })
    }

    override fun isFollowing(
        currentUserId: String,
        targetJobSeekerId: String,
        callback: (Boolean) -> Unit
    ) {
        ref.child(targetJobSeekerId).child("followers")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var following = false

                    if (snapshot.exists()) {
                        for (followerSnapshot in snapshot.children) {
                            val followerId = followerSnapshot.getValue(String::class.java)
                            if (followerId == currentUserId) {
                                following = true
                                break
                            }
                        }
                    }

                    callback(following)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false)
                }
            })
    }

    // ============================================
    // APPLIED JOBS FUNCTIONALITY METHODS
    // ============================================

    override fun applyForJob(
        appliedJobModel: AppliedJobModel,
        callback: (Boolean, String) -> Unit
    ) {
        val applicationId = appliedJobsRef.push().key ?: return callback(false, "Failed to generate application ID")

        val applicationWithId = appliedJobModel.copy(applicationId = applicationId)

        // Save application to AppliedJobs node
        appliedJobsRef.child(applicationId).setValue(applicationWithId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update JobSeeker's appliedJobs list
                    ref.child(appliedJobModel.jobSeekerId).child("appliedJobs")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val appliedJobsList = mutableListOf<String>()

                                if (snapshot.exists()) {
                                    for (jobSnapshot in snapshot.children) {
                                        val jobId = jobSnapshot.getValue(String::class.java)
                                        if (jobId != null) {
                                            appliedJobsList.add(jobId)
                                        }
                                    }
                                }

                                appliedJobsList.add(applicationId)

                                ref.child(appliedJobModel.jobSeekerId).child("appliedJobs")
                                    .setValue(appliedJobsList)
                                    .addOnCompleteListener { updateTask ->
                                        if (updateTask.isSuccessful) {
                                            callback(true, "Application submitted successfully")
                                        } else {
                                            callback(false, updateTask.exception?.message ?: "Failed to update profile")
                                        }
                                    }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                callback(false, error.message)
                            }
                        })
                } else {
                    callback(false, task.exception?.message ?: "Failed to submit application")
                }
            }
    }

    override fun getAppliedJobsByJobSeeker(
        jobSeekerId: String,
        callback: (Boolean, String, List<AppliedJobModel>?) -> Unit
    ) {
        appliedJobsRef.orderByChild("jobSeekerId").equalTo(jobSeekerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val appliedJobsList = mutableListOf<AppliedJobModel>()

                        for (data in snapshot.children) {
                            val appliedJob = data.getValue(AppliedJobModel::class.java)
                            if (appliedJob != null) {
                                appliedJobsList.add(appliedJob)
                            }
                        }

                        // Sort by timestamp (most recent first)
                        appliedJobsList.sortByDescending { it.appliedTimestamp }

                        callback(true, "Applied jobs fetched successfully", appliedJobsList)
                    } else {
                        callback(true, "No applied jobs found", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getAppliedJobsByStatus(
        jobSeekerId: String,
        status: String,
        callback: (Boolean, String, List<AppliedJobModel>?) -> Unit
    ) {
        appliedJobsRef.orderByChild("jobSeekerId").equalTo(jobSeekerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val appliedJobsList = mutableListOf<AppliedJobModel>()

                        for (data in snapshot.children) {
                            val appliedJob = data.getValue(AppliedJobModel::class.java)
                            if (appliedJob != null && appliedJob.status == status) {
                                appliedJobsList.add(appliedJob)
                            }
                        }

                        // Sort by timestamp (most recent first)
                        appliedJobsList.sortByDescending { it.appliedTimestamp }

                        callback(true, "Applied jobs fetched successfully", appliedJobsList)
                    } else {
                        callback(true, "No applied jobs found", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun updateApplicationStatus(
        applicationId: String,
        newStatus: String,
        callback: (Boolean, String) -> Unit
    ) {
        appliedJobsRef.child(applicationId).child("status").setValue(newStatus)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Application status updated to $newStatus")
                } else {
                    callback(false, task.exception?.message ?: "Failed to update status")
                }
            }
    }

    override fun withdrawApplication(
        applicationId: String,
        jobSeekerId: String,
        callback: (Boolean, String) -> Unit
    ) {
        // Update status to Withdrawn
        appliedJobsRef.child(applicationId).child("status").setValue("Withdrawn")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Optionally remove from JobSeeker's appliedJobs list
                    ref.child(jobSeekerId).child("appliedJobs")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val appliedJobsList = mutableListOf<String>()

                                if (snapshot.exists()) {
                                    for (jobSnapshot in snapshot.children) {
                                        val jobId = jobSnapshot.getValue(String::class.java)
                                        if (jobId != null && jobId != applicationId) {
                                            appliedJobsList.add(jobId)
                                        }
                                    }
                                }

                                ref.child(jobSeekerId).child("appliedJobs")
                                    .setValue(appliedJobsList)
                                    .addOnCompleteListener {
                                        callback(true, "Application withdrawn successfully")
                                    }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                callback(false, error.message)
                            }
                        })
                } else {
                    callback(false, task.exception?.message ?: "Failed to withdraw application")
                }
            }
    }

    override fun getApplicationById(
        applicationId: String,
        callback: (Boolean, String, AppliedJobModel?) -> Unit
    ) {
        appliedJobsRef.child(applicationId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val appliedJob = snapshot.getValue(AppliedJobModel::class.java)
                    if (appliedJob != null) {
                        callback(true, "Application fetched successfully", appliedJob)
                    } else {
                        callback(false, "Failed to parse application data", null)
                    }
                } else {
                    callback(false, "Application not found", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }
}
