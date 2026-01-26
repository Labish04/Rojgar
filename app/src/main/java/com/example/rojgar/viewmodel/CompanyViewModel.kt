package com.example.rojgar.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.repository.CompanyRepo
import com.google.firebase.auth.FirebaseUser

class CompanyViewModel(val repo: CompanyRepo) {

    private val _companyDetails = MutableLiveData<CompanyModel?>()
    val companyDetails: MutableLiveData<CompanyModel?> get() = _companyDetails

    private val _allCompanies = MutableLiveData<List<CompanyModel>>()
    val allCompanies: LiveData<List<CompanyModel>> get() = _allCompanies

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

    fun signInWithGoogle(
        idToken: String,
        fullName: String,
        email: String,
        photoUrl: String,
        callback: (Boolean, String, String?) -> Unit
    ) {
        repo.signInWithGoogle(idToken, fullName, email, photoUrl, callback)
    }

    fun getCompanyByEmail(
        email: String,
        callback: (Boolean, String, CompanyModel?) -> Unit
    ) {
        repo.getCompanyByEmail(email, callback)
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
            repo.getCompanyById(currentUser.uid) { success, message, companyModel ->
                _loading.value = false
                if (success && companyModel != null) {
                    _companyDetails.value = companyModel
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
    ) {
        repo.uploadCompanyProfileImage(context, imageUri, callback)
    }

    fun uploadCompanyCoverPhoto(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        repo.uploadCompanyCoverPhoto(context, imageUri, callback)
    }

    fun deleteAccount(
        companyId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.deleteAccount(companyId, callback)
    }

    fun checkAccountStatusByEmail(
        email: String,
        callback: (Boolean, String?, String) -> Unit
    ) {
        repo.checkAccountStatusByEmail(email, callback)
    }

    fun reactivateAccount(
        companyId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.reactivateAccount(companyId, callback)
    }

    fun deactivateAccount(
        companyId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.deactivateAccount(companyId, callback)
    }

    fun checkAccountStatus(
        companyId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.checkAccountStatus(companyId, callback)
    }

    fun fetchAllCompaniesForMap() {
        _loading.value = true
        repo.getAllCompany { success, message, list ->
            _loading.value = false
            if (success && list != null) {
                _allCompanies.value = list
            }
        }
    }
}