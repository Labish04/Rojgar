package com.example.rojgar.repository

import com.example.rojgar.model.SearchHistoryModel

interface SearchRepo {
    fun saveSearch(
        search: SearchHistoryModel,
        callback: (Boolean, String) -> Unit
    )

    fun getSearchHistory(
        userId: String,
        userType: String,
        limit: Int = 20,
        callback: (Boolean, String, List<SearchHistoryModel>?) -> Unit
    )

    fun deleteSearch(
        searchId: String,
        callback: (Boolean, String) -> Unit
    )

    fun clearAllSearchHistory(
        userId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getRecentSearches(
        userId: String,
        limit: Int = 5,
        callback: (Boolean, String, List<SearchHistoryModel>?) -> Unit
    )

    fun updateSearchResultCount(
        searchId: String,
        resultCount: Int,
        callback: (Boolean, String) -> Unit
    )
    fun deleteSearchHistory(
        userId: String,
        timestamp: Long,
        callback: (Boolean, String) -> Unit
    )
}