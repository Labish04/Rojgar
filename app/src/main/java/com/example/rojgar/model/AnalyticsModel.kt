package com.example.rojgar.model

data class JobAnalyticsMetrics(
    val jobId: String = "",
    val jobTitle: String = "",
    val totalApplications: Int = 0,
    val views: Int = 0,
    val saves: Int = 0,
    val shortlisted: Int = 0,
    val hired: Int = 0,
    val conversionRate: Float = 0f,
    val timeToHire: Int = 0, // days
    val postedDate: Long = System.currentTimeMillis(),
    val deadline: String = "",
    val category: String = "",
    val salary: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "jobId" to jobId,
            "jobTitle" to jobTitle,
            "totalApplications" to totalApplications,
            "views" to views,
            "saves" to saves,
            "shortlisted" to shortlisted,
            "hired" to hired,
            "conversionRate" to conversionRate,
            "timeToHire" to timeToHire,
            "postedDate" to postedDate,
            "deadline" to deadline,
            "category" to category,
            "salary" to salary
        )
    }
}

data class ConversionMetrics(
    val totalApplications: Int = 0,
    val totalShortlisted: Int = 0,
    val totalHired: Int = 0,
    val conversionRate: Float = 0f, // applications to hired
    val shortlistRate: Float = 0f // applications to shortlisted
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "totalApplications" to totalApplications,
            "totalShortlisted" to totalShortlisted,
            "totalHired" to totalHired,
            "conversionRate" to conversionRate,
            "shortlistRate" to shortlistRate
        )
    }
}

data class CategoryPerformance(
    val category: String = "",
    val jobCount: Int = 0,
    val totalApplications: Int = 0,
    val avgApplicationsPerJob: Float = 0f,
    val totalHires: Int = 0
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "category" to category,
            "jobCount" to jobCount,
            "totalApplications" to totalApplications,
            "avgApplicationsPerJob" to avgApplicationsPerJob,
            "totalHires" to totalHires
        )
    }
}

data class CompanyProfileAnalytics(
    val companyId: String = "",
    val companyName: String = "",
    val profileViews: Int = 0,
    val followers: Int = 0,
    val totalJobsPosted: Int = 0,
    val activeJobs: Int = 0,
    val totalApplicationsReceived: Int = 0,
    val totalHires: Int = 0,
    val avgTimeToHire: Int = 0, // days
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "companyId" to companyId,
            "companyName" to companyName,
            "profileViews" to profileViews,
            "followers" to followers,
            "totalJobsPosted" to totalJobsPosted,
            "activeJobs" to activeJobs,
            "totalApplicationsReceived" to totalApplicationsReceived,
            "totalHires" to totalHires,
            "avgTimeToHire" to avgTimeToHire,
            "lastUpdated" to lastUpdated
        )
    }
}

data class AnalyticsDashboard(
    val companyAnalytics: CompanyProfileAnalytics = CompanyProfileAnalytics(),
    val conversionMetrics: ConversionMetrics = ConversionMetrics(),
    val jobMetrics: List<JobAnalyticsMetrics> = emptyList(),
    val categoryPerformance: List<CategoryPerformance> = emptyList(),
    val topPerformingJobs: List<JobAnalyticsMetrics> = emptyList(),
    val bottomPerformingJobs: List<JobAnalyticsMetrics> = emptyList()
)
