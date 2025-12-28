package com.example.rojgar.viewmodel

import com.example.rojgar.model.LanguageModel
import com.example.rojgar.repository.LanguageRepo

class LanguageViewModel(private val repo: LanguageRepo) {

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
}