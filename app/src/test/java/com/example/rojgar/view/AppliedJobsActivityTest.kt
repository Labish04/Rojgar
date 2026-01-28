package com.example.rojgar.view

import com.example.rojgar.model.ApplicationModel
import com.example.rojgar.repository.ApplicationRepoImpl
import com.example.rojgar.viewmodel.ApplicationViewModel
import org.junit.Test
import org.junit.Assert.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Rule

class AppliedJobsActivityTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun formatApplicationDate_returnsJustNow_forRecentTimestamp() {
        val now = System.currentTimeMillis()
        val result = formatApplicationDate(now - 30000) // 30 seconds ago
        assertEquals("Just now", result)
    }

    @Test
    fun formatApplicationDate_returnsMinutes_forMinutesAgo() {
        val now = System.currentTimeMillis()
        val result = formatApplicationDate(now - 120000) // 2 minutes ago
        assertEquals("2m ago", result)
    }

    @Test
    fun formatApplicationDate_returnsHours_forHoursAgo() {
        val now = System.currentTimeMillis()
        val result = formatApplicationDate(now - 7200000) // 2 hours ago
        assertEquals("2h ago", result)
    }
}