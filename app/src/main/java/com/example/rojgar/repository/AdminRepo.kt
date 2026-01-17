package com.example.rojgar.repository

import com.example.rojgar.model.CompanyModel
import com.google.firebase.auth.FirebaseUser

interface AdminRepo {

    // Company Verification Management
    fun getUnverifiedCompanies(
        callback: (Boolean, String, List<CompanyModel>?) -> Unit
    )

    fun getPendingVerificationRequests(
        callback: (Boolean, String, List<CompanyModel>?) -> Unit
    )

    fun getCompanyVerificationDetails(
        companyId: String,
        callback: (Boolean, String, CompanyModel?) -> Unit
    )

    fun approveCompanyVerification(
        companyId: String,
        reviewedBy: String,
        callback: (Boolean, String) -> Unit
    )

    fun rejectCompanyVerification(
        companyId: String,
        reviewedBy: String,
        rejectionReason: String,
        callback: (Boolean, String) -> Unit
    )

    fun getAllCompanies(
        callback: (Boolean, String, List<CompanyModel>?) -> Unit
    )

    fun getVerifiedCompanies(
        callback: (Boolean, String, List<CompanyModel>?) -> Unit
    )

    fun getRejectedCompanies(
        callback: (Boolean, String, List<CompanyModel>?) -> Unit
    )

    fun searchCompanies(
        query: String,
        callback: (Boolean, String, List<CompanyModel>?) -> Unit
    )

    fun filterCompaniesByStatus(
        status: String,
        callback: (Boolean, String, List<CompanyModel>?) -> Unit
    )

    fun getVerificationStats(
        callback: (Boolean, String, Map<String, Int>?) -> Unit
    )

    // Admin can manage company accounts
    fun deactivateCompanyAccount(
        companyId: String,
        callback: (Boolean, String) -> Unit
    )

    fun reactivateCompanyAccount(
        companyId: String,
        callback: (Boolean, String) -> Unit
    )

    fun deleteCompanyAccount(
        companyId: String,
        callback: (Boolean, String) -> Unit
    )
}