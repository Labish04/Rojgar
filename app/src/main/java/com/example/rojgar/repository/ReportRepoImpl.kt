package com.example.rojgar.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import androidx.compose.runtime.mutableStateListOf
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.rojgar.model.ReportModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import java.util.concurrent.Executors

class ReportRepoImpl : ReportRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val reportsRef: DatabaseReference = database.getReference("Reports")
    private val companiesRef: DatabaseReference = database.getReference("Companys")

    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dtmprduic",
            "api_key" to "883843915169633",
            "api_secret" to "DhiLcks25VLVZCBhWgGvObdGGyE"
        )
    )

    override fun submitReport(
        report: ReportModel,
        callback: (Boolean, String) -> Unit
    ) {
        // Generate a unique report ID
        val reportKey = reportsRef.push().key
        if (reportKey == null) {
            callback(false, "Failed to generate report ID")
            return
        }

        // Create report with ID
        val reportWithId = report.copy(reportId = reportKey)

        // Save to Firebase
        reportsRef.child(reportKey).setValue(reportWithId.toMap())
            .addOnSuccessListener {
                // Also add report reference to company for easier querying
                companiesRef.child(report.reportedCompanyId).child("reports")
                    .child(reportKey).setValue(true)
                    .addOnSuccessListener {
                        callback(true, "Report submitted successfully. Our team will review it within 24 hours.")
                    }
                    .addOnFailureListener { e ->
                        callback(false, "Failed to update company references: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to submit report: ${e.message}")
            }
    }

    override fun uploadEvidenceImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                var fileName = getFileNameFromUri(context, imageUri)

                fileName = fileName?.substringBeforeLast(".") ?: "evidence_image"
                fileName = "report_evidence_${System.currentTimeMillis()}"

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

    override fun getReportsByCompanyId(
        companyId: String,
        callback: (Boolean, String, List<ReportModel>?) -> Unit
    ) {
        reportsRef.orderByChild("reportedCompanyId").equalTo(companyId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val reports = mutableStateListOf<ReportModel>()
                        for (data in snapshot.children) {
                            val report = data.getValue(ReportModel::class.java)
                            if (report != null) {
                                reports.add(report)
                            }
                        }
                        callback(true, "Reports fetched", reports)
                    } else {
                        callback(true, "No reports found", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getReportsByReporterId(
        reporterId: String,
        callback: (Boolean, String, List<ReportModel>?) -> Unit
    ) {
        reportsRef.orderByChild("reporterId").equalTo(reporterId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val reports = mutableStateListOf<ReportModel>()
                        for (data in snapshot.children) {
                            val report = data.getValue(ReportModel::class.java)
                            if (report != null) {
                                reports.add(report)
                            }
                        }
                        callback(true, "Reports fetched", reports)
                    } else {
                        callback(true, "No reports found", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getAllReports(
        callback: (Boolean, String, List<ReportModel>?) -> Unit
    ) {
        reportsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val allReports = mutableStateListOf<ReportModel>()
                    for (data in snapshot.children) {
                        val report = data.getValue(ReportModel::class.java)
                        if (report != null) {
                            allReports.add(report)
                        }
                    }
                    callback(true, "Reports fetched", allReports)
                } else {
                    callback(true, "No reports found", emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun updateReportStatus(
        reportId: String,
        status: String,
        adminNotes: String,
        resolvedBy: String,
        callback: (Boolean, String) -> Unit
    ) {
        val updates = hashMapOf<String, Any>(
            "status" to status,
            "adminNotes" to adminNotes,
            "updatedAt" to System.currentTimeMillis()
        )

        if (status == "resolved" || status == "dismissed") {
            updates["resolvedAt"] = System.currentTimeMillis()
            updates["resolvedBy"] = resolvedBy
        }

        reportsRef.child(reportId).updateChildren(updates)
            .addOnSuccessListener {
                callback(true, "Report status updated successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to update report: ${e.message}")
            }
    }

    private fun getFileNameFromUri(
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