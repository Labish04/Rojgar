package com.example.rojgar.repository

import com.example.rojgar.model.CompanyModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminRepoImpl : AdminRepo{

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val companiesRef = database.getReference("Companys")
    private val adminVerificationsRef = database.getReference("AdminVerifications")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())


    override fun getUnverifiedCompanies(callback: (Boolean, String, List<CompanyModel>?) -> Unit) {
        companiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val unverifiedCompanies = mutableListOf<CompanyModel>()
                    for (data in snapshot.children) {
                        val company = data.getValue(CompanyModel::class.java)
                        if (company != null &&
                            company.verificationStatus == "pending" &&
                            !company.isVerified) {
                            val companyWithId = company.copy(companyId = data.key ?: "")
                            unverifiedCompanies.add(companyWithId)
                        }
                    }
                    callback(true, "Unverified companies fetched", unverifiedCompanies)
                } else {
                    callback(false, "No companies found", emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun getPendingVerificationRequests(callback: (Boolean, String, List<CompanyModel>?) -> Unit) {
        companiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val pendingRequests = mutableListOf<CompanyModel>()
                    for (data in snapshot.children) {
                        val company = data.getValue(CompanyModel::class.java)
                        if (company != null &&
                            company.verificationStatus == "pending" &&
                            company.verificationRequestDate.isNotEmpty()) {
                            val companyWithId = company.copy(companyId = data.key ?: "")
                            pendingRequests.add(companyWithId)
                        }
                    }
                    // Sort by request date (newest first)
                    pendingRequests.sortByDescending { it.verificationRequestDate }
                    callback(true, "Pending verification requests fetched", pendingRequests)
                } else {
                    callback(false, "No pending requests found", emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun getCompanyVerificationDetails(
        companyId: String,
        callback: (Boolean, String, CompanyModel?) -> Unit
    ) {
        if (companyId.isEmpty()) {
            callback(false, "Invalid company ID", null)
            return
        }

        companiesRef.child(companyId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val company = snapshot.getValue(CompanyModel::class.java)
                        if (company != null) {
                            val companyWithId = company.copy(companyId = companyId)
                            callback(true, "Company details fetched", companyWithId)
                        } else {
                            callback(false, "Failed to parse company data", null)
                        }
                    } else {
                        callback(false, "Company not found", null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            }
        )
    }

    override fun approveCompanyVerification(
        companyId: String,
        reviewedBy: String,
        callback: (Boolean, String) -> Unit
    ) {
        val currentDate = dateFormat.format(Date())

        val updates = mapOf(
            "isVerified" to true,
            "verificationStatus" to "approved",
            "verificationReviewedDate" to currentDate,
            "verificationReviewedBy" to reviewedBy,
            "verificationRejectionReason" to ""
        )

        companiesRef.child(companyId).updateChildren(updates)
            .addOnSuccessListener {
                // Update admin verification record
                updateAdminVerificationRecord(companyId, "approved", reviewedBy)
                callback(true, "Company verification approved successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to approve verification: ${e.message}")
            }
    }

    private fun updateAdminVerificationRecord(
        companyId: String,
        status: String,
        reviewedBy: String,
        rejectionReason: String = ""
    ) {
        val currentDate = dateFormat.format(Date())

        adminVerificationsRef.child(companyId).updateChildren(
            mapOf(
                "status" to status,
                "reviewedAt" to currentDate,
                "reviewedBy" to reviewedBy,
                "rejectionReason" to rejectionReason
            )
        )
    }

    override fun rejectCompanyVerification(
        companyId: String,
        reviewedBy: String,
        rejectionReason: String,
        callback: (Boolean, String) -> Unit
    ) {
        val currentDate = dateFormat.format(Date())

        val updates = mapOf(
            "isVerified" to false,
            "verificationStatus" to "rejected",
            "verificationReviewedDate" to currentDate,
            "verificationReviewedBy" to reviewedBy,
            "verificationRejectionReason" to rejectionReason
        )

        companiesRef.child(companyId).updateChildren(updates)
            .addOnSuccessListener {
                // Update admin verification record
                updateAdminVerificationRecord(companyId, "rejected", reviewedBy, rejectionReason)
                callback(true, "Company verification rejected")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to reject verification: ${e.message}")
            }
    }

    override fun getAllCompanies(callback: (Boolean, String, List<CompanyModel>?) -> Unit) {
        companiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val allCompanies = mutableListOf<CompanyModel>()
                    for (data in snapshot.children) {
                        val company = data.getValue(CompanyModel::class.java)
                        if (company != null) {
                            val companyWithId = company.copy(companyId = data.key ?: "")
                            allCompanies.add(companyWithId)
                        }
                    }
                    // Sort by verification status
                    allCompanies.sortBy { it.verificationStatus }
                    callback(true, "All companies fetched", allCompanies)
                } else {
                    callback(false, "No companies found", emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun getVerifiedCompanies(callback: (Boolean, String, List<CompanyModel>?) -> Unit) {
        companiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val verifiedCompanies = mutableListOf<CompanyModel>()
                    for (data in snapshot.children) {
                        val company = data.getValue(CompanyModel::class.java)
                        if (company != null && company.isVerified && company.verificationStatus == "approved") {
                            val companyWithId = company.copy(companyId = data.key ?: "")
                            verifiedCompanies.add(companyWithId)
                        }
                    }
                    callback(true, "Verified companies fetched", verifiedCompanies)
                } else {
                    callback(false, "No verified companies found", emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun getRejectedCompanies(callback: (Boolean, String, List<CompanyModel>?) -> Unit) {
        companiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val rejectedCompanies = mutableListOf<CompanyModel>()
                    for (data in snapshot.children) {
                        val company = data.getValue(CompanyModel::class.java)
                        if (company != null && company.verificationStatus == "rejected") {
                            val companyWithId = company.copy(companyId = data.key ?: "")
                            rejectedCompanies.add(companyWithId)
                        }
                    }
                    callback(true, "Rejected companies fetched", rejectedCompanies)
                } else {
                    callback(false, "No rejected companies found", emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun searchCompanies(
        query: String,
        callback: (Boolean, String, List<CompanyModel>?) -> Unit
    ) {
        companiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val searchResults = mutableListOf<CompanyModel>()
                    for (data in snapshot.children) {
                        val company = data.getValue(CompanyModel::class.java)
                        if (company != null) {
                            if (company.companyName.contains(query, ignoreCase = true) ||
                                company.companyEmail.contains(query, ignoreCase = true) ||
                                company.companyIndustry.contains(query, ignoreCase = true)) {
                                val companyWithId = company.copy(companyId = data.key ?: "")
                                searchResults.add(companyWithId)
                            }
                        }
                    }
                    callback(true, "Search results fetched", searchResults)
                } else {
                    callback(false, "No companies found", emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun filterCompaniesByStatus(
        status: String,
        callback: (Boolean, String, List<CompanyModel>?) -> Unit
    ) {
        companiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val filteredCompanies = mutableListOf<CompanyModel>()
                    for (data in snapshot.children) {
                        val company = data.getValue(CompanyModel::class.java)
                        if (company != null && company.verificationStatus == status) {
                            val companyWithId = company.copy(companyId = data.key ?: "")
                            filteredCompanies.add(companyWithId)
                        }
                    }
                    callback(true, "Companies filtered by status", filteredCompanies)
                } else {
                    callback(false, "No companies found", emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun getVerificationStats(
        callback: (Boolean, String, Map<String, Int>?) -> Unit
    ) {
        companiesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var total = 0
                    var pending = 0
                    var approved = 0
                    var rejected = 0
                    var active = 0

                    for (data in snapshot.children) {
                        total++
                        val company = data.getValue(CompanyModel::class.java)
                        if (company != null) {
                            when (company.verificationStatus) {
                                "pending" -> pending++
                                "approved" -> {
                                    approved++
                                    if (company.isActive) active++
                                }
                                "rejected" -> rejected++
                            }
                        }
                    }

                    val stats = mapOf(
                        "total" to total,
                        "pending" to pending,
                        "approved" to approved,
                        "rejected" to rejected,
                        "active" to active
                    )
                    callback(true, "Statistics fetched", stats)
                } else {
                    callback(false, "No companies found", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun deactivateCompanyAccount(
        companyId: String,
        callback: (Boolean, String) -> Unit
    ) {
        companiesRef.child(companyId).child("isActive").setValue(false)
            .addOnSuccessListener {
                callback(true, "Company account deactivated")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to deactivate account: ${e.message}")
            }
    }

    override fun reactivateCompanyAccount(
        companyId: String,
        callback: (Boolean, String) -> Unit
    ) {
        companiesRef.child(companyId).child("isActive").setValue(true)
            .addOnSuccessListener {
                callback(true, "Company account reactivated")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to reactivate account: ${e.message}")
            }
    }

    override fun deleteCompanyAccount(
        companyId: String,
        callback: (Boolean, String) -> Unit
    ) {
        companiesRef.child(companyId).removeValue()
            .addOnSuccessListener {
                // Also delete from authentication if needed
                callback(true, "Company account deleted")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to delete account: ${e.message}")
            }
    }
}