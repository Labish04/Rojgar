package com.example.rojgar.repository

import com.example.rojgar.model.AnalyticsDashboard
import com.example.rojgar.model.JobAnalyticsMetrics
import com.example.rojgar.model.ConversionMetrics
import com.example.rojgar.model.CategoryPerformance
import com.example.rojgar.model.CompanyProfileAnalytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class AnalyticsRepoImpl : AnalyticsRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val jobRef: DatabaseReference = database.getReference("Job")
    // Application data is stored under "Applications" (see ApplicationRepoImpl)
    private val applicationRef: DatabaseReference = database.getReference("Applications")
    private val companyRef: DatabaseReference = database.getReference("Company")
    private val executor = Executors.newSingleThreadExecutor()

    override fun getCompanyDashboard(
        companyId: String,
        callback: (Boolean, String, AnalyticsDashboard?) -> Unit
    ) {
        executor.execute {
            try {
                var profileAnalytics: CompanyProfileAnalytics? = null
                var conversionMetrics: ConversionMetrics? = null
                var jobMetrics: List<JobAnalyticsMetrics>? = null
                var categoryPerformance: List<CategoryPerformance>? = null

                // Fetch all data in parallel
                val barrier = object {
                    var completed = 0
                    fun checkComplete() {
                        completed++
                        if (completed == 4) {
                            if (profileAnalytics != null && conversionMetrics != null && 
                                jobMetrics != null && categoryPerformance != null) {
                                val dashboard = AnalyticsDashboard(
                                    companyAnalytics = profileAnalytics!!,
                                    conversionMetrics = conversionMetrics!!,
                                    jobMetrics = jobMetrics!!,
                                    categoryPerformance = categoryPerformance!!,
                                    topPerformingJobs = jobMetrics!!.sortedByDescending { it.totalApplications }.take(5),
                                    bottomPerformingJobs = jobMetrics!!.sortedBy { it.totalApplications }.take(5)
                                )
                                callback(true, "Dashboard loaded", dashboard)
                            } else {
                                callback(false, "Failed to load dashboard data", null)
                            }
                        }
                    }
                }

                getCompanyProfileAnalytics(companyId) { success, msg, analytics ->
                    if (success) profileAnalytics = analytics
                    barrier.checkComplete()
                }

                getConversionMetrics(companyId) { success, msg, metrics ->
                    if (success) conversionMetrics = metrics
                    barrier.checkComplete()
                }

                getJobMetrics(companyId) { success, msg, metrics ->
                    if (success) jobMetrics = metrics
                    barrier.checkComplete()
                }

                getCategoryPerformance(companyId) { success, msg, categories ->
                    if (success) categoryPerformance = categories
                    barrier.checkComplete()
                }
            } catch (e: Exception) {
                callback(false, e.message ?: "Unknown error", null)
            }
        }
    }

    override fun getJobMetrics(
        companyId: String,
        callback: (Boolean, String, List<JobAnalyticsMetrics>?) -> Unit
    ) {
        jobRef.orderByChild("companyId").equalTo(companyId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val jobSnapshots = snapshot.children.toList()
                    
                    if (jobSnapshots.isEmpty()) {
                        callback(true, "No jobs found", emptyList())
                        return
                    }

                    val jobMetricsList = mutableListOf<JobAnalyticsMetrics>()
                    var pendingJobsCount = jobSnapshots.size
                    var hasError = false

                    for (jobSnapshot in jobSnapshots) {
                        val jobId = jobSnapshot.child("postId").value?.toString() ?: continue
                        val jobTitle = jobSnapshot.child("title").value?.toString() ?: ""
                        val category = jobSnapshot.child("categories").value?.toString() ?: ""
                        val salary = jobSnapshot.child("salary").value?.toString() ?: ""
                        val postedDate = jobSnapshot.child("timestamp").value as? Long ?: System.currentTimeMillis()
                        val deadline = jobSnapshot.child("deadline").value?.toString() ?: ""

                        // Get applications for this job using single-value event
                        // Applications use field name "postId" to reference the job (ApplicationModel.postId)
                        applicationRef.orderByChild("postId").equalTo(jobId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(appSnapshot: DataSnapshot) {
                                    if (hasError) return
                                    
                                    var totalApplications = 0
                                    var shortlisted = 0
                                    var hired = 0
                                    var rejected = 0
                                    var totalTimeToHire = 0
                                    var hireCount = 0

                                    for (appChild in appSnapshot.children) {
                                        totalApplications++
                                        val status = appChild.child("status").value?.toString() ?: ""
                                        val statusNorm = status.lowercase()
                                        val appliedDate = appChild.child("appliedDate").value as? Long ?: System.currentTimeMillis()
                                        val hireDate = appChild.child("hireDate").value as? Long ?: System.currentTimeMillis()

                                        when (statusNorm) {
                                            "shortlisted" -> shortlisted++
                                            // Accept both "hired" and "accepted" as hires
                                            "hired", "accepted" -> {
                                                hired++
                                                hireCount++
                                                totalTimeToHire += ((hireDate - appliedDate) / (1000 * 60 * 60 * 24)).toInt()
                                            }
                                            // Treat "rejected" or "declined" as rejected
                                            "rejected", "declined" -> rejected++
                                        }
                                    }

                                    val conversionRate = if (totalApplications > 0) {
                                        (hired.toFloat() / totalApplications * 100).roundToInt()
                                    } else {
                                        0
                                    }

                                    val timeToHire = if (hireCount > 0) totalTimeToHire / hireCount else 0

                                    synchronized(jobMetricsList) {
                                        jobMetricsList.add(
                                        JobAnalyticsMetrics(
                                                jobId = jobId,
                                                jobTitle = jobTitle,
                                                totalApplications = totalApplications,
                                                views = 0, // To be implemented with event tracking
                                                saves = 0, // To be implemented with event tracking
                                                shortlisted = shortlisted,
                                                hired = hired,
                                                rejected = rejected,
                                                conversionRate = conversionRate.toFloat(),
                                                timeToHire = timeToHire,
                                                postedDate = postedDate,
                                                deadline = deadline,
                                                category = category,
                                                salary = salary
                                            )
                                        )
                                        
                                        pendingJobsCount--
                                        
                                        // Check if all jobs processed
                                        if (pendingJobsCount == 0) {
                                            callback(true, "Job metrics loaded", jobMetricsList)
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    if (!hasError) {
                                        hasError = true
                                        callback(false, error.message, null)
                                    }
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getConversionMetrics(
        companyId: String,
        callback: (Boolean, String, ConversionMetrics?) -> Unit
    ) {
        getJobMetrics(companyId) { success, msg, jobMetrics ->
            if (success && jobMetrics != null) {
                var totalApplications = 0
                var totalShortlisted = 0
                var totalHired = 0

                var totalRejected = 0
                for (job in jobMetrics) {
                    totalApplications += job.totalApplications
                    totalShortlisted += job.shortlisted
                    totalHired += job.hired
                    totalRejected += job.rejected
                }

                val conversionRate = if (totalApplications > 0) {
                    (totalHired.toFloat() / totalApplications * 100)
                } else {
                    0f
                }

                val shortlistRate = if (totalApplications > 0) {
                    (totalShortlisted.toFloat() / totalApplications * 100)
                } else {
                    0f
                }

                val metrics = ConversionMetrics(
                    totalApplications = totalApplications,
                    totalShortlisted = totalShortlisted,
                    totalHired = totalHired,
                    totalRejected = totalRejected,
                    conversionRate = conversionRate,
                    shortlistRate = shortlistRate
                )

                callback(true, "Conversion metrics loaded", metrics)
            } else {
                callback(false, msg, null)
            }
        }
    }

    override fun getCategoryPerformance(
        companyId: String,
        callback: (Boolean, String, List<CategoryPerformance>?) -> Unit
    ) {
        getJobMetrics(companyId) { success, msg, jobMetrics ->
            if (success && jobMetrics != null) {
                val categoryMap = mutableMapOf<String, CategoryPerformance>()

                for (job in jobMetrics) {
                    val category = job.category
                    val existing = categoryMap[category] ?: CategoryPerformance(category = category)

                    categoryMap[category] = existing.copy(
                        jobCount = existing.jobCount + 1,
                        totalApplications = existing.totalApplications + job.totalApplications,
                        totalHires = existing.totalHires + job.hired,
                        avgApplicationsPerJob = (existing.totalApplications + job.totalApplications).toFloat() / (existing.jobCount + 1)
                    )
                }

                val categoryList = categoryMap.values.sortedByDescending { it.totalApplications }
                callback(true, "Category performance loaded", categoryList)
            } else {
                callback(false, msg, null)
            }
        }
    }

    override fun getCompanyProfileAnalytics(
        companyId: String,
        callback: (Boolean, String, CompanyProfileAnalytics?) -> Unit
    ) {
        getJobMetrics(companyId) { success, msg, jobMetrics ->
            if (success && jobMetrics != null) {
                companyRef.child(companyId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val companyName = snapshot.child("companyName").value?.toString() ?: ""
                        val followers = snapshot.child("followers").value as? Int ?: 0
                        val profileViews = snapshot.child("profileViews").value as? Int ?: 0

                        var totalApplications = 0
                        var totalHires = 0
                        var totalTimeToHire = 0
                        var hireCount = 0

                        for (job in jobMetrics) {
                            totalApplications += job.totalApplications
                            totalHires += job.hired
                            if (job.hired > 0) {
                                totalTimeToHire += job.timeToHire
                                hireCount++
                            }
                        }

                        val avgTimeToHire = if (hireCount > 0) totalTimeToHire / hireCount else 0

                        val analytics = CompanyProfileAnalytics(
                            companyId = companyId,
                            companyName = companyName,
                            profileViews = profileViews,
                            followers = followers,
                            totalJobsPosted = jobMetrics.size,
                            activeJobs = jobMetrics.size,
                            totalApplicationsReceived = totalApplications,
                            totalHires = totalHires,
                            avgTimeToHire = avgTimeToHire,
                            lastUpdated = System.currentTimeMillis()
                        )

                        callback(true, "Profile analytics loaded", analytics)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        callback(false, error.message, null)
                    }
                })
            } else {
                callback(false, msg, null)
            }
        }
    }

    override fun getTopPerformingJobs(
        companyId: String,
        limit: Int,
        callback: (Boolean, String, List<JobAnalyticsMetrics>?) -> Unit
    ) {
        getJobMetrics(companyId) { success, msg, jobMetrics ->
            if (success && jobMetrics != null) {
                val topJobs = jobMetrics.sortedByDescending { it.totalApplications }.take(limit)
                callback(true, "Top jobs loaded", topJobs)
            } else {
                callback(false, msg, null)
            }
        }
    }

    override fun trackJobView(jobId: String, callback: (Boolean, String) -> Unit) {
        jobRef.child(jobId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentViews = snapshot.child("views").value as? Int ?: 0
                jobRef.child(jobId).child("views").setValue(currentViews + 1).addOnCompleteListener {
                    callback(it.isSuccessful, if (it.isSuccessful) "View tracked" else it.exception?.message ?: "Error")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message)
            }
        })
    }

    override fun trackJobSave(jobId: String, callback: (Boolean, String) -> Unit) {
        jobRef.child(jobId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentSaves = snapshot.child("saves").value as? Int ?: 0
                jobRef.child(jobId).child("saves").setValue(currentSaves + 1).addOnCompleteListener {
                    callback(it.isSuccessful, if (it.isSuccessful) "Save tracked" else it.exception?.message ?: "Error")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message)
            }
        })
    }

    override fun trackProfileView(companyId: String, callback: (Boolean, String) -> Unit) {
        companyRef.child(companyId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentViews = snapshot.child("profileViews").value as? Int ?: 0
                companyRef.child(companyId).child("profileViews").setValue(currentViews + 1).addOnCompleteListener {
                    callback(it.isSuccessful, if (it.isSuccessful) "Profile view tracked" else it.exception?.message ?: "Error")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message)
            }
        })
    }

    override fun trackApplicationStatus(
        jobId: String,
        applicationId: String,
        status: String,
        callback: (Boolean, String) -> Unit
    ) {
        applicationRef.child(applicationId).child("status").setValue(status).addOnCompleteListener {
            callback(it.isSuccessful, if (it.isSuccessful) "Status updated" else it.exception?.message ?: "Error")
        }
    }
}
