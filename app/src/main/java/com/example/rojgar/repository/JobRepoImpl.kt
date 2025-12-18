package com.example.rojgar.repository

import com.example.rojgar.model.JobModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class JobRepoImpl : JobRepo{
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref: DatabaseReference = database.getReference("Job")
    // Job Post Implementation
    override fun createJobPost(
        jobPost: JobModel,
        callback: (Boolean, String) -> Unit
    ) {
        val postId = ref.push().key ?: UUID.randomUUID().toString()
        val postWithId = jobPost.copy(postId = postId)

        ref.child(postId).setValue(postWithId).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Job post created successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun updateJobPost(
        jobPost: JobModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(jobPost.postId).setValue(jobPost).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Job post updated successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun deleteJobPost(
        postId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(postId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Job post deleted successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun getJobPostsByCompanyId(
        companyId: String,
        callback: (Boolean, String, List<JobModel>?) -> Unit
    ) {
        ref.orderByChild("companyId").equalTo(companyId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val jobPosts = mutableListOf<JobModel>()
                        for (data in snapshot.children) {
                            val post = data.getValue(JobModel::class.java)
                            if (post != null) {
                                jobPosts.add(post)
                            }
                        }
                        // Sort by timestamp (newest first)
                        jobPosts.sortByDescending { it.timestamp }
                        callback(true, "Job posts fetched", jobPosts)
                    } else {
                        callback(true, "No job posts found", emptyList())
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getJobPostById(
        postId: String,
        callback: (Boolean, String, JobModel?) -> Unit
    ) {
        ref.child(postId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val jobPost = snapshot.getValue(JobModel::class.java)
                    callback(true, "Job post fetched", jobPost)
                } else {
                    callback(false, "Job post not found", null)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
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

}