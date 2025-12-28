package com.example.rojgar.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.rojgar.model.EducationModel
import com.example.rojgar.model.SkillModel
import com.example.rojgar.repository.EducationRepo

class EducationViewModel(private val repo: EducationRepo) {

    private val _education = MutableLiveData< EducationModel?>()
    val education: LiveData<EducationModel?> = _education

    private val _allEducations = MutableLiveData<List<EducationModel>?>()
    val allEducations : MutableLiveData<List<EducationModel>?> get() = _allEducations

    fun addEducation(
        education: EducationModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.addEducation(education, callback)
    }

    fun getEducationsByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<EducationModel>?) -> Unit
    ) {
        repo.getEducationsByJobSeekerId(jobSeekerId, callback)
    }

    fun updateEducation(
        educationId: String,
        updatedEducation: EducationModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.updateEducation(educationId, updatedEducation, callback)
    }

    fun deleteEducation(
        educationId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.deleteEducation(educationId, callback)
    }

    fun getEducationById(
        educationId: String,
        callback: (Boolean, String, EducationModel?) -> Unit
    ) {
        repo.getEducationById(educationId, callback)
    }

    fun fetchEducationsByJobSeekerId(jobSeekerId: String) {

        Log.d("EducationViewModel", "Fetching educations for jobSeekerId: $jobSeekerId")

        repo.getEducationsByJobSeekerId(jobSeekerId) { success, message, educations ->

            Log.d("EducationViewModel", "Fetch result - Success: $success, Message: $message")
            Log.d("EducationViewModel", "Educations count: ${educations?.size}")

            if (success) {
                _allEducations.postValue(educations ?: emptyList())
                Log.d("EducationViewModel", "Educations set to LiveData: ${educations?.size} items")
            } else {
                _allEducations.postValue(emptyList())
                Log.e("EducationViewModel", "Failed to fetch educations: $message")
            }
        }
    }
}