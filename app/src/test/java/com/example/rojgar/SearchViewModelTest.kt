package com.example.rojgar

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.rojgar.model.SearchHistoryModel
import com.example.rojgar.repository.SearchRepo
import com.example.rojgar.viewmodel.SearchViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class SearchViewModelTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private val searchRepo: SearchRepo = mock()
    private val viewModel = SearchViewModel(searchRepo)

    @Test
    fun saveSearch_success_updatesLoadingAndCallsCallback() {
        val searchHistoryModel = SearchHistoryModel(
            userId = "user123",
            query = "Android Developer",
            timestamp = System.currentTimeMillis()
        )

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Success")
            null
        }.`when`(searchRepo).saveSearch(eq(searchHistoryModel), any())

        var callbackSuccess = false
        var callbackMessage = ""

        viewModel.saveSearch(searchHistoryModel) { success, message ->
            callbackSuccess = success
            callbackMessage = message
        }

        assertEquals(true, callbackSuccess)
        assertEquals("Success", callbackMessage)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun getRecentSearches_success_updatesLiveData() {
        val userId = "user123"
        val expectedSearches = listOf(
            SearchHistoryModel(userId = userId, query = "Java"),
            SearchHistoryModel(userId = userId, query = "Kotlin")
        )

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, List<SearchHistoryModel>?) -> Unit>(2)
            callback(true, "Success", expectedSearches)
            null
        }.`when`(searchRepo).getRecentSearches(eq(userId), any(), any())

        viewModel.getRecentSearches(userId)

        assertEquals(expectedSearches, viewModel.recentSearches.value)
    }

    @Test
    fun clearAllSearchHistory_success_clearsLiveData() {
        val userId = "user123"

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Success")
            null
        }.`when`(searchRepo).clearAllSearchHistory(eq(userId), any())

        viewModel.clearAllSearchHistory(userId) { success, message ->
            assertEquals(true, success)
        }

        assertEquals(emptyList<SearchHistoryModel>(), viewModel.searchHistory.value)
        assertEquals(emptyList<SearchHistoryModel>(), viewModel.recentSearches.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun deleteSearchHistory_success_callsRepo() {
        val userId = "user123"
        val timestamp = 123456789L

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(true, "Deleted successfully")
            null
        }.`when`(searchRepo).deleteSearchHistory(eq(userId), eq(timestamp), any())

        var callbackCalled = false
        viewModel.deleteSearchHistory(userId, timestamp) { success, message ->
            callbackCalled = true
            assertEquals(true, success)
            assertEquals("Deleted successfully", message)
        }

        verify(searchRepo).deleteSearchHistory(eq(userId), eq(timestamp), any())
        assertEquals(true, callbackCalled)
    }
}
