package com.example.rojgar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.repository.AdminRepo
import com.google.firebase.auth.FirebaseUser

class AdminViewModel(private val repository: AdminRepo) : ViewModel() {

    private val _currentAdmin = MutableLiveData<FirebaseUser?>()
    val currentAdmin: LiveData<FirebaseUser?> = _currentAdmin

    private val _unverifiedCompanies = MutableLiveData<List<CompanyModel>>()
    val unverifiedCompanies: LiveData<List<CompanyModel>> = _unverifiedCompanies

    private val _pendingVerificationRequests = MutableLiveData<List<CompanyModel>>()
    val pendingVerificationRequests: LiveData<List<CompanyModel>> = _pendingVerificationRequests

    private val _allCompanies = MutableLiveData<List<CompanyModel>>()
    val allCompanies: LiveData<List<CompanyModel>> = _allCompanies

    private val _verifiedCompanies = MutableLiveData<List<CompanyModel>>()
    val verifiedCompanies: LiveData<List<CompanyModel>> = _verifiedCompanies

    private val _rejectedCompanies = MutableLiveData<List<CompanyModel>>()
    val rejectedCompanies: LiveData<List<CompanyModel>> = _rejectedCompanies

    private val _verificationStats = MutableLiveData<Map<String, Int>>()
    val verificationStats: LiveData<Map<String, Int>> = _verificationStats

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _success = MutableLiveData<String>()
    val success: LiveData<String> = _success

    fun fetchUnverifiedCompanies() {
        _loading.value = true
        repository.getUnverifiedCompanies { success, message, companies ->
            _loading.value = false
            if (success && companies != null) {
                _unverifiedCompanies.value = companies
            } else {
                _error.value = message
                _unverifiedCompanies.value = emptyList()
            }
        }
    }

    fun fetchPendingVerificationRequests() {
        _loading.value = true
        repository.getPendingVerificationRequests { success, message, companies ->
            _loading.value = false
            if (success && companies != null) {
                _pendingVerificationRequests.value = companies
            } else {
                _error.value = message
                _pendingVerificationRequests.value = emptyList()
            }
        }
    }

    fun fetchAllCompanies() {
        _loading.value = true
        repository.getAllCompanies { success, message, companies ->
            _loading.value = false
            if (success && companies != null) {
                _allCompanies.value = companies
            } else {
                _error.value = message
                _allCompanies.value = emptyList()
            }
        }
    }

    fun fetchVerifiedCompanies() {
        _loading.value = true
        repository.getVerifiedCompanies { success, message, companies ->
            _loading.value = false
            if (success && companies != null) {
                _verifiedCompanies.value = companies
            } else {
                _error.value = message
                _verifiedCompanies.value = emptyList()
            }
        }
    }

    fun fetchRejectedCompanies() {
        _loading.value = true
        repository.getRejectedCompanies { success, message, companies ->
            _loading.value = false
            if (success && companies != null) {
                _rejectedCompanies.value = companies
            } else {
                _error.value = message
                _rejectedCompanies.value = emptyList()
            }
        }
    }

    fun fetchVerificationStats() {
        _loading.value = true
        repository.getVerificationStats { success, message, stats ->
            _loading.value = false
            if (success && stats != null) {
                _verificationStats.value = stats
            } else {
                _error.value = message
            }
        }
    }

    fun approveCompanyVerification(companyId: String, reviewedBy: String) {
        _loading.value = true
        repository.approveCompanyVerification(companyId, reviewedBy) { success, message ->
            _loading.value = false
            if (success) {
                _success.value = message
                // Refresh the lists
                fetchUnverifiedCompanies()
                fetchPendingVerificationRequests()
                fetchAllCompanies()
                fetchVerificationStats()
            } else {
                _error.value = message
            }
        }
    }

    fun rejectCompanyVerification(companyId: String, reviewedBy: String, rejectionReason: String) {
        _loading.value = true
        repository.rejectCompanyVerification(companyId, reviewedBy, rejectionReason) { success, message ->
            _loading.value = false
            if (success) {
                _success.value = message
                // Refresh the lists
                fetchUnverifiedCompanies()
                fetchPendingVerificationRequests()
                fetchAllCompanies()
                fetchVerificationStats()
            } else {
                _error.value = message
            }
        }
    }

    fun searchCompanies(query: String, callback: (List<CompanyModel>?) -> Unit) {
        repository.searchCompanies(query) { success, message, companies ->
            if (success) {
                callback(companies)
            } else {
                _error.value = message
                callback(null)
            }
        }
    }

    fun filterCompaniesByStatus(status: String, callback: (List<CompanyModel>?) -> Unit) {
        repository.filterCompaniesByStatus(status) { success, message, companies ->
            if (success) {
                callback(companies)
            } else {
                _error.value = message
                callback(null)
            }
        }
    }

    fun deactivateCompanyAccount(companyId: String) {
        _loading.value = true
        repository.deactivateCompanyAccount(companyId) { success, message ->
            _loading.value = false
            if (success) {
                _success.value = message
                fetchAllCompanies()
                fetchVerificationStats()
            } else {
                _error.value = message
            }
        }
    }

    fun reactivateCompanyAccount(companyId: String) {
        _loading.value = true
        repository.reactivateCompanyAccount(companyId) { success, message ->
            _loading.value = false
            if (success) {
                _success.value = message
                fetchAllCompanies()
                fetchVerificationStats()
            } else {
                _error.value = message
            }
        }
    }

    fun deleteCompanyAccount(companyId: String) {
        _loading.value = true
        repository.deleteCompanyAccount(companyId) { success, message ->
            _loading.value = false
            if (success) {
                _success.value = message
                fetchAllCompanies()
                fetchVerificationStats()
            } else {
                _error.value = message
            }
        }
    }

    fun clearMessages() {
        _error.value = ""
        _success.value = ""
    }
}