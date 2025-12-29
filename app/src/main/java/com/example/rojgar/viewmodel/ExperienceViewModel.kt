package com.example.rojgar.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.rojgar.model.EducationModel
import com.example.rojgar.model.ExperienceModel
import com.example.rojgar.repository.ExperienceRepo

class ExperienceViewModel(private val repo: ExperienceRepo) {

    private val _experience = MutableLiveData< ExperienceModel?>()
    val experience: LiveData<ExperienceModel?> = _experience

    private val _allExperiences = MutableLiveData<List<ExperienceModel>?>()
    val allExperiences : MutableLiveData<List<ExperienceModel>?> get() = _allExperiences

    fun addExperience(
        experience: ExperienceModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.addExperience(experience, callback)
    }

    fun getExperiencesByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<ExperienceModel>?) -> Unit
    ) {
        repo.getExperiencesByJobSeekerId(jobSeekerId, callback)
    }

    fun updateExperience(
        experienceId: String,
        updatedExperience: ExperienceModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.updateExperience(experienceId, updatedExperience, callback)
    }

    fun deleteExperience(
        experienceId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.deleteExperience(experienceId, callback)
    }

    fun getExperienceById(
        experienceId: String,
        callback: (Boolean, String, ExperienceModel?) -> Unit
    ) {
        repo.getExperienceById(experienceId, callback)
    }

    fun fetchExperiencesByJobSeekerId(jobSeekerId: String) {

        Log.d("ExperienceViewModel", "Fetching experiences for jobSeekerId: $jobSeekerId")

        repo.getExperiencesByJobSeekerId(jobSeekerId) { success, message, experiences ->


            if (success) {
                _allExperiences.postValue(experiences ?: emptyList())
                Log.d("ExperienceViewModel", "Experiences set to LiveData: ${experiences?.size} items")
            } else {
                _allExperiences.postValue(emptyList())
                Log.e("ExperienceViewModel", "Failed to fetch experiences: $message")
            }
        }
    }
}