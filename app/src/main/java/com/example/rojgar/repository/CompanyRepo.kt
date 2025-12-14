package com.example.rojgar.repository

import com.example.rojgar.model.CompanyModel
import com.google.firebase.auth.FirebaseUser

interface CompanyRepo {
    fun register(
        email : String,
        password : String,
        callback : (Boolean, String, String) ->Unit
    )

    fun login(
        email : String,
        password : String,
        callback : (Boolean, String) ->Unit
    )

    fun addCompanyToDatabase(
        companyId : String,
        model : CompanyModel,
        callback : (Boolean, String) ->Unit
    )

    fun getCurrentCompany() : FirebaseUser?

    fun getCompanyById(
        companyId : String,
        callback : (Boolean, String, CompanyModel?) ->Unit
    )

    fun getAllCompany(
        callback : (Boolean, String, List<CompanyModel>?) ->Unit
    )

    fun logout(
        companyId : String,
        callback : (Boolean, String) ->Unit
    )

    fun forgetPassword(
        email : String,
        callback : (Boolean, String) ->Unit
    )
}