package com.example.rojgar.repository

import androidx.compose.runtime.mutableStateListOf
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
        ref.child(jobSeekerId).addValueEventListener(object : ValueEventListener{
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

    override fun getAllJobPosts(
        callback: (Boolean, String, List<JobModel>?) -> Unit) {
        val jobPostRef = FirebaseDatabase.getInstance().getReference("JobPosts")
        jobPostRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    callback(false, "No job posts found", emptyList())
                    return
                }

                val jobList = mutableListOf<JobModel>()

                for (postSnapshot in snapshot.children) {
                    val job = postSnapshot.getValue(JobModel::class.java)
                    if (job != null) {
                        jobList.add(
                            job.copy(postId = postSnapshot.key ?: "")
                        )
                    }
                }

                callback(true, "Jobs fetched successfully", jobList)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getCompanyDetails(
        companyId: String,
        callback: (Boolean, String, String?, String?) -> Unit
    ) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("companies")
            .child(companyId)

        ref.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val companyName =
                        snapshot.child("companyName").value as? String

                    val companyProfileImage =
                        snapshot.child("companyProfileImage").value as? String

                    callback(
                        true,
                        "Company details fetched successfully",
                        companyName,
                        companyProfileImage
                    )
                } else {
                    callback(false, "Company not found", null, null)
                }
            }
            .addOnFailureListener {
                callback(
                    false,
                    it.message ?: "Failed to fetch company details",
                    null,
                    null
                )
            }
    }


}