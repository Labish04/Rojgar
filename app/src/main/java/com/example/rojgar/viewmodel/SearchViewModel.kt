package com.example.rojgar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rojgar.model.SearchHistoryModel
import com.example.rojgar.repository.SearchRepo

class SearchViewModel(private val searchRepo: SearchRepo) : ViewModel() {

    private val _searchHistory = MutableLiveData<List<SearchHistoryModel>>()
    val searchHistory: LiveData<List<SearchHistoryModel>> = _searchHistory

    private val _recentSearches = MutableLiveData<List<SearchHistoryModel>>()
    val recentSearches: LiveData<List<SearchHistoryModel>> = _recentSearches

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun saveSearch(search: SearchHistoryModel, callback: (Boolean, String) -> Unit) {
        _isLoading.value = true
        searchRepo.saveSearch(search) { success, message ->
            _isLoading.value = false
            if (!success) {
                _errorMessage.value = message
            }
            callback(success, message)
        }
    }

    fun getSearchHistory(userId: String, userType: String, limit: Int = 20) {
        _isLoading.value = true
        searchRepo.getSearchHistory(userId, userType, limit) { success, message, searches ->
            _isLoading.value = false
            if (success && searches != null) {
                _searchHistory.value = searches
            } else {
                _errorMessage.value = message
                _searchHistory.value = emptyList()
            }
        }
    }

    fun deleteSearch(searchId: String, callback: (Boolean, String) -> Unit) {
        _isLoading.value = true
        searchRepo.deleteSearch(searchId) { success, message ->
            _isLoading.value = false
            if (!success) {
                _errorMessage.value = message
            }
            callback(success, message)
        }
    }

    fun clearAllSearchHistory(userId: String, callback: (Boolean, String) -> Unit) {
        _isLoading.value = true
        searchRepo.clearAllSearchHistory(userId) { success, message ->
            _isLoading.value = false
            if (success) {
                _searchHistory.value = emptyList()
                _recentSearches.value = emptyList()
            } else {
                _errorMessage.value = message
            }
            callback(success, message)
        }
    }

    fun getRecentSearches(userId: String, limit: Int = 5) {
        searchRepo.getRecentSearches(userId, limit) { success, message, searches ->
            if (success && searches != null) {
                _recentSearches.value = searches
            } else {
                _errorMessage.value = message
                _recentSearches.value = emptyList()
            }
        }
    }

    fun updateSearchResultCount(searchId: String, resultCount: Int) {
        searchRepo.updateSearchResultCount(searchId, resultCount) { success, message ->
            if (!success) {
                _errorMessage.value = message
            }
        }
    }
}