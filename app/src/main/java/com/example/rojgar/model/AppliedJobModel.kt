package com.example.rojgar.model

data class AppliedJobModel(
    val applicationId: String = "",
    val jobSeekerId: String = "",
    val jobId: String = "",
    val companyId: String = "",
    val jobTitle: String = "",
    val companyName: String = "",
    val status: String = "Pending", // Pending, Viewed, Shortlisted, Offered, Hired, Rejected, Withdrawn
    val location: String = "",
    val openings: Int = 0,
    val appliedDate: String = "",
    val appliedTimestamp: Long = 0L,
    val resumeUrl: String = "",
    val coverLetter: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "applicationId" to applicationId,
            "jobSeekerId" to jobSeekerId,
            "jobId" to jobId,
            "companyId" to companyId,
            "jobTitle" to jobTitle,
            "companyName" to companyName,
            "status" to status,
            "location" to location,
            "openings" to openings,
            "appliedDate" to appliedDate,
            "appliedTimestamp" to appliedTimestamp,
            "resumeUrl" to resumeUrl,
            "coverLetter" to coverLetter
        )
    }
}