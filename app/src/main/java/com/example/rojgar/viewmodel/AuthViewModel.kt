package com.example.rojgar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rojgar.repository.UserRepo

class AuthViewModel(private val repo: UserRepo) : ViewModel() {

    private val _userRole = MutableLiveData<String>()
    val userRole: LiveData<String> = _userRole

    fun loadUserRole() {
        repo.getCurrentUserRole { role ->
            _userRole.postValue(role?: "No User Available")
        }
    }
}