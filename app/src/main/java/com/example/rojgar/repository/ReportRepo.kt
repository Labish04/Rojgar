package com.example.rojgar.repository

import android.content.Context
import android.net.Uri
import com.example.rojgar.model.ReportModel

interface ReportRepo {
    fun submitReport(
        report: ReportModel,
        callback: (Boolean, String) -> Unit
    )

    fun uploadEvidenceImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    )

    fun getReportsByCompanyId(
        companyId: String,
        callback: (Boolean, String, List<ReportModel>?) -> Unit
    )

    fun getReportsByReporterId(
        reporterId: String,
        callback: (Boolean, String, List<ReportModel>?) -> Unit
    )

    fun getAllReports(
        callback: (Boolean, String, List<ReportModel>?) -> Unit
    )

    fun updateReportStatus(
        reportId: String,
        status: String,
        adminNotes: String,
        resolvedBy: String,
        callback: (Boolean, String) -> Unit
    )
}