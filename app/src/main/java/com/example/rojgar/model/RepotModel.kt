package com.example.rojgar.model

import java.util.Date

data class ReportModel(
    val reportId: String = "",
    val reporterId: String = "", // ID of user who reported
    val reporterType: String = "", // "JobSeeker" or "Company"
    val reporterName: String = "",
    val reportedCompanyId: String = "",
    val reportedCompanyName: String = "",
    val reportCategory: String = "", // spam, inappropriate, fake, harassment, other
    val reportReason: String = "",
    val description: String = "",
    val evidenceUrls: List<String> = emptyList(), // URLs of screenshots/evidence
    val status: String = "pending", // pending, reviewing, resolved, dismissed
    val adminNotes: String = "",
    val createdAt: Long = Date().time,
    val updatedAt: Long = Date().time,
    val resolvedAt: Long = 0L,
    val resolvedBy: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "reportId" to reportId,
            "reporterId" to reporterId,
            "reporterType" to reporterType,
            "reporterName" to reporterName,
            "reportedCompanyId" to reportedCompanyId,
            "reportedCompanyName" to reportedCompanyName,
            "reportCategory" to reportCategory,
            "reportReason" to reportReason,
            "description" to description,
            "evidenceUrls" to evidenceUrls,
            "status" to status,
            "adminNotes" to adminNotes,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "resolvedAt" to resolvedAt,
            "resolvedBy" to resolvedBy
        )
    }
}