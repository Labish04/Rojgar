package com.example.rojgar.model

data class ApplicationModel(
    val applicationId: String = "",
    val postId: String = "",
    val companyId: String = "",
    val jobSeekerId: String = "",
    val jobSeekerName: String = "",
    val jobSeekerEmail: String = "",
    val jobSeekerPhone: String = "",
    val jobSeekerProfile: String = "",
    val appliedDate: Long = System.currentTimeMillis(),
    val status: String = "Pending",
    val coverLetter: String = "",
    val resumeUrl: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "applicationId" to applicationId,
            "postId" to postId,
            "companyId" to companyId,
            "jobSeekerId" to jobSeekerId,
            "jobSeekerName" to jobSeekerName,
            "jobSeekerEmail" to jobSeekerEmail,
            "jobSeekerPhone" to jobSeekerPhone,
            "jobSeekerProfile" to jobSeekerProfile,
            "appliedDate" to appliedDate,
            "status" to status,
            "coverLetter" to coverLetter,
            "resumeUrl" to resumeUrl
        )
    }
}