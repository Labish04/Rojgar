package com.example.rojgar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rojgar.repository.FollowRepo

class FollowViewModelFactory(private val followRepo: FollowRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FollowViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FollowViewModel(followRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
