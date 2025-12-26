package com.example.rojgar.repository

import com.example.rojgar.model.PreferenceModel

interface PreferenceRepo {
    fun savePreference(
        preference: PreferenceModel,
        callback: (Boolean, String) -> Unit
    )

    fun updatePreference(
        preference: PreferenceModel,
        callback: (Boolean, String) -> Unit
    )

    fun getPreferenceByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, PreferenceModel?) -> Unit
    )

    fun deletePreference(
        preferenceId: String,
        callback: (Boolean, String) -> Unit
    )
}