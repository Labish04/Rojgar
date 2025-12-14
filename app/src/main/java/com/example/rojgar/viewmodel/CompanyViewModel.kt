package com.example.rojgar.viewmodel

import com.example.rojgar.model.CompanyModel
import com.example.rojgar.repository.CompanyRepo
import com.google.firebase.auth.FirebaseUser

class CompanyViewModel (val repo: CompanyRepo){
    fun register(
        email : String,
        password : String,
        callback : (Boolean, String, String) ->Unit
    )
    {
        repo.register(email,password,callback)
    }

    fun login(
        email : String,
        password : String,
        callback : (Boolean, String) ->Unit
    ){
        repo.login(email,password,callback)
    }

    fun addCompanyToDatabase(
        companyId : String,
        model : CompanyModel,
        callback : (Boolean, String) ->Unit
    ){
        repo.addCompanyToDatabase(companyId,model,callback)
    }

    fun getCurrentCompany() : FirebaseUser?{
        return repo.getCurrentCompany()
    }

    fun getCompanyById(
        companyId : String,
        callback : (Boolean, String, CompanyModel?) ->Unit
    ){
        repo.getCompanyById(companyId,callback)
    }

    fun getAllCompany(
        callback : (Boolean, String, List<CompanyModel>?) ->Unit
    ){
        repo.getAllCompany(callback)
    }

    fun logout(
        companyId : String,
        callback : (Boolean, String) ->Unit
    ){
        repo.logout(companyId,callback)
    }

    fun forgetPassword(
        email : String,
        callback : (Boolean, String) ->Unit
    ){
        repo.forgetPassword(email,callback)
    }
}