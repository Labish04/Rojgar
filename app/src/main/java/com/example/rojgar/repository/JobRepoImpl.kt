package com.example.rojgar.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.rojgar.model.JobModel
import com.example.rojgar.model.PreferenceModel
import com.example.rojgar.model.JobSeekerModel
import com.example.rojgar.model.SkillModel
import com.example.rojgar.model.ExperienceModel
import com.example.rojgar.model.EducationModel
import com.example.rojgar.recommendation.EnhancedJobRecommendationEngine
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.Executors

class JobRepoImpl : JobRepo {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref: DatabaseReference = database.getReference("Job")

    private val recommendationEngine = EnhancedJobRecommendationEngine()

    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dtmprduic",
            "api_key" to "883843915169633",
            "api_secret" to "DhiLcks25VLVZCBhWgGvObdGGyE"
        )
    )

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
                        callback(true, "No job posts found", emptyList())
                        return
                    }

                    for (child in snapshot.children) {
                        val job = child.getValue(JobModel::class.java)
                        if (job != null) {
                            list.add(job.copy(postId = child.key ?: job.postId))
                        }
                    }

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
                        jobList.add(job.copy(postId = postSnapshot.key ?: job.postId))
                    }
                }

                jobList.sortByDescending { it.timestamp }
                callback(true, "Fetched ${jobList.size} jobs successfully", jobList)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    /**
     * ENHANCED RECOMMENDATION SYSTEM
     * Uses the new EnhancedJobRecommendationEngine with comprehensive scoring
     */
    override fun getRecommendedJobs(
        preference: PreferenceModel,
        callback: (Boolean, String, List<JobModel>?) -> Unit
    ) {
        // First, get all jobs
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(jobSnapshot: DataSnapshot) {
                if (!jobSnapshot.exists()) {
                    callback(true, "No jobs found", emptyList())
                    return
                }

                val allJobs = mutableListOf<JobModel>()
                for (postSnapshot in jobSnapshot.children) {
                    val job = postSnapshot.getValue(JobModel::class.java)
                    if (job != null) {
                        allJobs.add(job.copy(postId = postSnapshot.key ?: job.postId))
                    }
                }

                // Get current user data for comprehensive recommendations
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    // If no user, use basic preference-only matching
                    val basicRecommendations = getBasicRecommendations(allJobs, preference)
                    callback(true, "Found ${basicRecommendations.size} recommended jobs", basicRecommendations)
                    return
                }

                // Fetch user profile data
                fetchUserProfileData(currentUser.uid, allJobs, preference, callback)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    /**
     * Fetch comprehensive user profile data for advanced recommendations
     */
    private fun fetchUserProfileData(
        userId: String,
        allJobs: List<JobModel>,
        preference: PreferenceModel,
        callback: (Boolean, String, List<JobModel>?) -> Unit
    ) {
        val jobSeekerRef = database.getReference("JobSeekers").child(userId)
        val skillsRef = database.getReference("Skills")
        val experienceRef = database.getReference("Experience")
        val educationRef = database.getReference("Education")

        var jobSeeker: JobSeekerModel? = null
        var skills: List<SkillModel>? = null
        var experiences: List<ExperienceModel>? = null
        var education: List<EducationModel>? = null
        var fetchCount = 0

        val checkComplete = {
            fetchCount++
            if (fetchCount == 4) {
                // All data fetched, generate recommendations
                val scoredJobs = recommendationEngine.getRecommendedJobs(
                    allJobs = allJobs,
                    jobSeeker = jobSeeker ?: JobSeekerModel(),
                    preference = preference,
                    skills = skills,
                    experiences = experiences,
                    education = education
                )

                val recommendedJobs = scoredJobs.map { it.job }
                callback(
                    true,
                    "Found ${recommendedJobs.size} personalized recommendations",
                    recommendedJobs
                )
            }
        }

        // Fetch JobSeeker
        jobSeekerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                jobSeeker = snapshot.getValue(JobSeekerModel::class.java)
                checkComplete()
            }
            override fun onCancelled(error: DatabaseError) {
                checkComplete()
            }
        })

        // Fetch Skills
        skillsRef.orderByChild("jobSeekerId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val skillList = mutableListOf<SkillModel>()
                    for (child in snapshot.children) {
                        child.getValue(SkillModel::class.java)?.let { skillList.add(it) }
                    }
                    skills = skillList
                    checkComplete()
                }
                override fun onCancelled(error: DatabaseError) {
                    checkComplete()
                }
            })

        // Fetch Experience
        experienceRef.orderByChild("jobSeekerId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val expList = mutableListOf<ExperienceModel>()
                    for (child in snapshot.children) {
                        child.getValue(ExperienceModel::class.java)?.let { expList.add(it) }
                    }
                    experiences = expList
                    checkComplete()
                }
                override fun onCancelled(error: DatabaseError) {
                    checkComplete()
                }
            })

        // Fetch Education
        educationRef.orderByChild("jobSeekerId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val eduList = mutableListOf<EducationModel>()
                    for (child in snapshot.children) {
                        child.getValue(EducationModel::class.java)?.let { eduList.add(it) }
                    }
                    education = eduList
                    checkComplete()
                }
                override fun onCancelled(error: DatabaseError) {
                    checkComplete()
                }
            })
    }

    /**
     * Basic recommendation fallback (when user data is not available)
     */
    private fun getBasicRecommendations(
        allJobs: List<JobModel>,
        preference: PreferenceModel
    ): List<JobModel> {
        val scoredJobs = allJobs.mapNotNull { job ->
            var score = 0

            // Category match
            if (job.categories.any { jobCategory ->
                    preference.categories.any { prefCategory ->
                        jobCategory.equals(prefCategory, true)
                    }
                }) {
                score += 3
            }

            // Job type match
            if (preference.availabilities.any {
                    it.equals(job.jobType, true)
                }) {
                score += 2
            }

            // Title match
            if (preference.titles.any { prefTitle ->
                    job.title.contains(prefTitle, true) ||
                            job.position.contains(prefTitle, true)
                }) {
                score += 3
            }

            if (score > 0) job to score else null
        }

        return scoredJobs
            .sortedByDescending { it.second }
            .map { it.first }
    }

    override fun uploadHiringBanner(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                var fileName = getFileNameFromUri(context, imageUri)

                fileName = fileName?.substringBeforeLast(".") ?: "uploaded_image"

                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", fileName,
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

    override fun getFileNameFromUri(
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
        return fileName
    }
}