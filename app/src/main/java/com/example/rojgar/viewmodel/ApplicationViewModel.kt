package com.example.rojgar.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rojgar.model.ApplicationModel
import com.example.rojgar.repository.ApplicationRepo

class ApplicationViewModel(private val repo: ApplicationRepo) : ViewModel() {

    private val _applyResult = MutableLiveData<Pair<Boolean, String>>()
    val applyResult: MutableLiveData<Pair<Boolean, String>> get() = _applyResult

    private val _applications = MutableLiveData<List<ApplicationModel>>()
    val applications: MutableLiveData<List<ApplicationModel>> get() = _applications

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> get() = _loading

    fun applyForJob(application: ApplicationModel) {
        _loading.value = true
        repo.applyForJob(application) { success, message ->
            _loading.value = false
            _applyResult.value = Pair(success, message)
        }
    }

    fun getApplicationsByJobSeeker(jobSeekerId: String) {
        _loading.value = true
        repo.getApplicationsByJobSeeker(jobSeekerId) { success, message, data ->
            _loading.value = false
            if (success && data != null) {
                _applications.value = data
            } else {
                _applications.value = emptyList()
            }
        }
    }

    fun getApplicationsByCompany(companyId: String) {
        _loading.value = true
        repo.getApplicationsByCompany(companyId) { success, message, data ->
            _loading.value = false
            if (success && data != null) {
                _applications.value = data
            } else {
                _applications.value = emptyList()
            }
        }
    }

    fun updateApplicationStatus(applicationId: String, status: String) {
        repo.updateApplicationStatus(applicationId, status) { success, message ->
            _applyResult.value = Pair(success, message)
        }
    }

}