// file name: HelpSupportViewModel.kt
package com.example.rojgar.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rojgar.model.HelpSupportModel
import com.example.rojgar.repository.HelpSupportRepo
import com.example.rojgar.repository.HelpSupportRepoImpl
import kotlinx.coroutines.launch

class HelpSupportViewModel(private val repo: HelpSupportRepo = HelpSupportRepoImpl()) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _submitSuccess = MutableLiveData<Boolean>()
    val submitSuccess: LiveData<Boolean> get() = _submitSuccess

    private val _submitMessage = MutableLiveData<String>()
    val submitMessage: LiveData<String> get() = _submitMessage

    private val _myRequests = MutableLiveData<List<HelpSupportModel>>()
    val myRequests: LiveData<List<HelpSupportModel>> get() = _myRequests

    private val _screenshotUrl = MutableLiveData<String?>()
    val screenshotUrl: LiveData<String?> get() = _screenshotUrl

    private val _uploadingScreenshot = MutableLiveData<Boolean>()
    val uploadingScreenshot: LiveData<Boolean> get() = _uploadingScreenshot

    fun submitHelpRequest(
        userId: String,
        userType: String,
        userEmail: String,
        userName: String,
        problemType: String,
        priority: String,
        description: String,
        screenshotUrl: String = ""
    ) {
        if (description.isEmpty()) {
            _submitMessage.value = "Please describe your problem"
            return
        }

        if (problemType.isEmpty()) {
            _submitMessage.value = "Please select a problem type"
            return
        }

        if (priority.isEmpty()) {
            _submitMessage.value = "Please select priority"
            return
        }

        _isLoading.value = true

        val helpRequest = HelpSupportModel(
            userId = userId,
            userType = userType,
            userEmail = userEmail,
            userName = userName,
            problemType = problemType,
            priority = priority,
            description = description,
            screenshotUrl = screenshotUrl
        )

        viewModelScope.launch {
            repo.submitHelpRequest(helpRequest) { success, message ->
                _isLoading.postValue(false)
                _submitSuccess.postValue(success)
                _submitMessage.postValue(message)
            }
        }
    }

    fun uploadScreenshot(context: Context, imageUri: Uri, userId: String) {
        _uploadingScreenshot.value = true
        viewModelScope.launch {
            repo.uploadScreenshot(context, imageUri, userId) { url ->
                _uploadingScreenshot.postValue(false)
                _screenshotUrl.postValue(url)
            }
        }
    }

    fun getMyHelpRequests(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            repo.getUserHelpRequests(userId) { success, message, requests ->
                _isLoading.postValue(false)
                if (success) {
                    _myRequests.postValue(requests ?: emptyList())
                }
            }
        }
    }

    fun getAllHelpRequests() {
        _isLoading.value = true
        viewModelScope.launch {
            repo.getAllHelpRequests { success, message, requests ->
                _isLoading.postValue(false)
                if (success) {
                    _myRequests.postValue(requests ?: emptyList())
                }
            }
        }
    }

    fun updateRequestStatus(
        requestId: String,
        status: String,
        adminNotes: String,
        resolvedBy: String
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            repo.updateRequestStatus(requestId, status, adminNotes, resolvedBy) { success, message ->
                _isLoading.postValue(false)
                _submitSuccess.postValue(success)
                _submitMessage.postValue(message)
            }
        }
    }

    fun clearScreenshot() {
        _screenshotUrl.value = null
    }

    fun resetSubmitState() {
        _submitSuccess.value = false
        _submitMessage.value = ""
    }
}