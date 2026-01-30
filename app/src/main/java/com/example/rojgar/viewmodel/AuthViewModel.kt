package com.example.rojgar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rojgar.repository.UserRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repo: UserRepo) : ViewModel() {

    private val _userRole = MutableStateFlow("No User Available")
    val userRole: StateFlow<String> = _userRole

    fun loadUserRole() {
        viewModelScope.launch {
            repo.getCurrentUserRole { role ->
                _userRole.value = role ?: "No User Available"
            }
        }
    }
}