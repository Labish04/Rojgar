
package com.example.rojgar.repository

import com.example.rojgar.model.LanguageModel

interface LanguageRepo {
    fun addLanguage(
        languageModel: LanguageModel,
        callback: (Boolean, String) -> Unit
    )

    fun updateLanguage(
        languageId: String,
        languageModel: LanguageModel,
        callback: (Boolean, String) -> Unit
    )

    fun deleteLanguage(
        languageId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getLanguagesByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<LanguageModel>?) -> Unit
    )

    fun getLanguageById(
        languageId: String,
        callback: (Boolean, String, LanguageModel?) -> Unit
    )
}
