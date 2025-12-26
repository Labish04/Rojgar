package com.example.rojgar.repository

import com.example.rojgar.model.JobModel
import com.example.rojgar.model.PreferenceModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class JobRepoImpl : JobRepo {
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
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<JobModel>()

                    if (!snapshot.exists()) {
                        // No data found - return empty list
                        callback(true, "No job posts found", emptyList())
                        return
                    }

                    for (child in snapshot.children) {
                        val job = child.getValue(JobModel::class.java)
                        if (job != null) {
                            // IMPORTANT: Use the Firebase key as postId
                            list.add(job.copy(postId = child.key ?: job.postId))
                        }
                    }

                    // Sort by timestamp (newest first)
                    list.sortByDescending { it.timestamp }

                    callback(true, "Fetched ${list.size} job posts", list)
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
                    if (jobPost != null) {
                        // Ensure postId is set correctly
                        callback(
                            true,
                            "Job post fetched",
                            jobPost.copy(postId = snapshot.key ?: jobPost.postId)
                        )
                    } else {
                        callback(false, "Failed to parse job post", null)
                    }
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
        callback: (Boolean, String, List<JobModel>?) -> Unit
    ) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    callback(true, "No job posts found", emptyList())
                    return
                }

                val jobList = mutableListOf<JobModel>()

                for (postSnapshot in snapshot.children) {
                    val job = postSnapshot.getValue(JobModel::class.java)
                    if (job != null) {
                        // IMPORTANT: Use the Firebase key as postId
                        jobList.add(job.copy(postId = postSnapshot.key ?: job.postId))
                    }
                }

                // Sort by timestamp (newest first)
                jobList.sortByDescending { it.timestamp }

                callback(true, "Fetched ${jobList.size} jobs successfully", jobList)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getRecommendedJobs(
        preference: PreferenceModel,
        callback: (Boolean, String, List<JobModel>?) -> Unit
    ) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    callback(true, "No jobs found", emptyList())
                    return
                }

                val scoredJobs = mutableListOf<Pair<JobModel, Int>>()

                for (postSnapshot in snapshot.children) {
                    val job = postSnapshot.getValue(JobModel::class.java)
                    if (job != null) {
                        val jobWithId =
                            job.copy(postId = postSnapshot.key ?: job.postId)

                        var score = 0

                        // 🔹 Category match (list)
                        if (job.categories.any { it.equals(preference.category, true) }) {
                            score += 3
                        }

                        // 🔹 Job type match
                        if (job.jobType.equals(preference.availability, true)) {
                            score += 2
                        }

                        // 🔹 Title / position match
                        if (
                            job.title.contains(preference.title, true) ||
                            job.position.contains(preference.title, true)
                        ) {
                            score += 2
                        }

                        // 🔹 Skills match (comma-separated)
                        val jobSkills = job.skills.split(",").map { it.trim().lowercase() }
                        val userSkills =
                            preference.title.split(",").map { it.trim().lowercase() }

                        if (jobSkills.any { it in userSkills }) {
                            score += 3
                        }

                        if (score > 0) {
                            scoredJobs.add(jobWithId to score)
                        }
                    }
                }

                val recommendedJobs = scoredJobs
                    .sortedByDescending { it.second }
                    .map { it.first }

                callback(
                    true,
                    "Found ${recommendedJobs.size} recommended jobs",
                    recommendedJobs
                )
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }
}