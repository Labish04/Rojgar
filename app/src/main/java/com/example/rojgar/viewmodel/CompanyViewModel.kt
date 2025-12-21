package com.example.rojgar.viewmodel

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


//    fun uploadImage(
//        imageUri: Uri,
//        callback: (Boolean, String, String) -> Unit
//    ) {
//        repo.uploadImage(imageUri, callback)
//    }
}