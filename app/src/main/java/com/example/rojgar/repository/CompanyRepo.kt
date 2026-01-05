package com.example.rojgar.repository

import android.content.Context
import android.net.Uri
import com.example.rojgar.model.CompanyModel
import com.google.firebase.auth.FirebaseUser

interface CompanyRepo {
    fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    )

    fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    )

    fun addCompanyToDatabase(
        companyId: String,
        model: CompanyModel,
        callback: (Boolean, String) -> Unit
    )

    fun getCurrentCompany(): FirebaseUser?

    fun getCompanyById(
        companyId: String,
        callback: (Boolean, String, CompanyModel?) -> Unit
    )

    fun getAllCompany(
        callback: (Boolean, String, List<CompanyModel>?) -> Unit
    )

    fun logout(
        companyId: String,
        callback: (Boolean, String) -> Unit
    )

    fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    )

    fun getCompanyDetails(
        companyId: String,
        callback: (Boolean, String, CompanyModel?) -> Unit
    )

    fun uploadRegistrationDocument(
        companyId: String,
        imageUri: Uri,
        callback: (Boolean, String) -> Unit
    )

    fun uploadCompanyProfileImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    )

    fun uploadCompanyCoverPhoto(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    )

    fun getFileNameFromUri(context: Context, imageUri: Uri): String?

    // NEW METHODS FOR ACCOUNT DEACTIVATION/REACTIVATION
    fun deactivateAccount(
        companyId: String,
        callback: (Boolean, String) -> Unit
    )

    fun reactivateAccount(
        companyId: String,
        callback: (Boolean, String) -> Unit
    )

    fun checkAccountStatus(
        companyId: String,
        callback: (Boolean, String) -> Unit
    )

    fun checkAccountStatusByEmail(
        email: String,
        callback: (Boolean, String?, String) -> Unit
    )

    fun updateCompanyProfile(
        model: CompanyModel,
        callback: (Boolean, String) -> Unit
    )
}