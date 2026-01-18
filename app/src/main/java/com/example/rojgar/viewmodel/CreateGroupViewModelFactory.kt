package com.example.rojgar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rojgar.repository.CompanyRepo
import com.example.rojgar.repository.FollowRepo
import com.example.rojgar.repository.JobSeekerRepo
import com.example.rojgar.repository.UserRepo

class CreateGroupViewModelFactory(
    private val userRepo: UserRepo,
    private val followRepo: FollowRepo,
    private val companyRepo: CompanyRepo,
    private val jobSeekerRepo: JobSeekerRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateGroupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateGroupViewModel().apply {
                initRepositories(userRepo, followRepo, companyRepo, jobSeekerRepo)
                loadCurrentUser()
            } as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
