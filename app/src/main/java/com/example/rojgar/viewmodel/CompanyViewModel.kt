package com.example.rojgar.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.model.JobModel
import com.example.rojgar.repository.CompanyRepo
import com.google.firebase.auth.FirebaseUser

class CompanyViewModel(val repo: CompanyRepo) {

    private val _companyDetails = MutableLiveData<CompanyModel?>()
    val companyDetails: MutableLiveData<CompanyModel?> get() = _companyDetails

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> get() = _loading

    fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        repo.register(email, password, callback)
    }

    fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.login(email, password, callback)
    }

    fun addCompanyToDatabase(
        companyId: String,
        model: CompanyModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.addCompanyToDatabase(companyId, model, callback)
    }

    fun getCurrentCompany(): FirebaseUser? {
        return repo.getCurrentCompany()
    }

    fun getCompanyById(
        companyId: String,
        callback: (Boolean, String, CompanyModel?) -> Unit
    ) {
        repo.getCompanyById(companyId, callback)
    }

    fun getAllCompany(
        callback: (Boolean, String, List<CompanyModel>?) -> Unit
    ) {
        repo.getAllCompany(callback)
    }

    fun logout(
        companyId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.logout(companyId, callback)
    }

    fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.forgetPassword(email, callback)
    }


    fun getCompanyDetails(companyId: String) {
        _loading.postValue(true)
        repo.getCompanyDetails(companyId) { success, message, data ->
            _loading.postValue(false)
            if (success) {
                _companyDetails.postValue(data)
            } else {
                _companyDetails.postValue(null)
            }
        }
    }

    fun fetchCurrentCompany() {
        _loading.value = true

        val currentUser = repo.getCurrentCompany()
        if (currentUser != null) {
            repo.getCompanyById(currentUser.uid) { success, message, jobSeekerModel ->
                _loading.value = false
                if (success && jobSeekerModel != null) {
                    _companyDetails.value = jobSeekerModel
                } else {
                    _companyDetails.value = null
                }
            }
        } else {
            _loading.value = false
            _companyDetails.value = null
        }
    }

    fun uploadCompanyProfileImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ){
        repo.uploadCompanyProfileImage(context, imageUri, callback)

    }

    fun uploadCompanyCoverPhoto(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ){
        repo.uploadCompanyCoverPhoto(context, imageUri, callback)
    }
}