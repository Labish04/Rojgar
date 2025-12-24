package com.example.rojgar.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rojgar.model.PreferenceModel
import com.example.rojgar.repository.PreferenceRepo
import com.example.rojgar.repository.PreferenceRepoImpl

class PreferenceViewModel : ViewModel() {
    private val repo: PreferenceRepo = PreferenceRepoImpl()

    private val _preferenceData = MutableLiveData<PreferenceModel?>()
    val preferenceData: MutableLiveData<PreferenceModel?> get() = _preferenceData

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> get() = _loading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: MutableLiveData<String> get() = _errorMessage

    fun savePreference(
        preference: PreferenceModel,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.value = true
        repo.savePreference(preference) { success, message ->
            _loading.value = false
            if (success) {
                _preferenceData.value = preference
            } else {
                _errorMessage.value = message
            }
            callback(success, message)
        }
    }

    fun updatePreference(
        preference: PreferenceModel,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.value = true
        repo.updatePreference(preference) { success, message ->
            _loading.value = false
            if (success) {
                _preferenceData.value = preference
            } else {
                _errorMessage.value = message
            }
            callback(success, message)
        }
    }

    fun getPreference(jobSeekerId: String) {
        _loading.value = true
        repo.getPreferenceByJobSeekerId(jobSeekerId) { success, message, preference ->
            _loading.value = false
            if (success && preference != null) {
                _preferenceData.value = preference
            } else {
                _preferenceData.value = null
                _errorMessage.value = message
            }
        }
    }

    fun deletePreference(
        preferenceId: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.value = true
        repo.deletePreference(preferenceId) { success, message ->
            _loading.value = false
            if (success) {
                _preferenceData.value = null
            }
            callback(success, message)
        }
    }
}