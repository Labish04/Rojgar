package com.example.rojgar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.rojgar.model.LanguageModel
import com.example.rojgar.model.TrainingModel
import com.example.rojgar.repository.LanguageRepo

class LanguageViewModel(private val repo: LanguageRepo) {

    private val _language = MutableLiveData< LanguageModel?>()
    val language: LiveData<LanguageModel?> = _language

    private val _allLanguages = MutableLiveData<List<LanguageModel>?>()
    val allLanguages : MutableLiveData<List<LanguageModel>?> get() = _allLanguages

    fun addLanguage(
        languageModel: LanguageModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.addLanguage(languageModel, callback)
    }

    fun updateLanguage(
        languageId: String,
        languageModel: LanguageModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.updateLanguage(languageId, languageModel, callback)
    }

    fun deleteLanguage(
        languageId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.deleteLanguage(languageId, callback)
    }

    fun getLanguagesByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<LanguageModel>?) -> Unit
    ) {
        repo.getLanguagesByJobSeekerId(jobSeekerId, callback)
    }

    fun getLanguageById(
        languageId: String,
        callback: (Boolean, String, LanguageModel?) -> Unit
    ) {
        repo.getLanguageById(languageId, callback)
    }

    fun fetchLanguagesByJobSeekerId(jobSeekerId: String) {
        repo.getLanguagesByJobSeekerId(jobSeekerId) { success, message, experiences ->
            if (success) {
                _allLanguages.postValue(experiences ?: emptyList())
            } else {
                _allLanguages.postValue(emptyList())
            }
        }
    }
}