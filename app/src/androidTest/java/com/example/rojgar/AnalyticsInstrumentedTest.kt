package com.example.rojgar

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rojgar.model.*
import com.example.rojgar.repository.AnalyticsRepo
import com.example.rojgar.repository.ApplicationRepo
import com.example.rojgar.repository.FollowRepo
import com.example.rojgar.view.AnalyticsScreen
import com.example.rojgar.viewmodel.AnalyticsViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*

@RunWith(AndroidJUnit4::class)
class AnalyticsInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun testAnalyticsScreen_displaysData() {
        // Mocks
        val app = ApplicationProvider.getApplicationContext<Application>()
        val analyticsRepo = mock<AnalyticsRepo>()
        val followRepo = mock<FollowRepo>()
        val applicationRepo = mock<ApplicationRepo>()

        // Data
        val companyId = "test_company"
        val dashboardData = AnalyticsDashboard(
            jobMetrics = listOf(JobAnalyticsMetrics(jobId = "job1", jobTitle = "Software Engineer", totalApplications = 5, hired = 1)),
            conversionMetrics = ConversionMetrics(totalApplications = 100, totalHired = 5),
            categoryPerformance = listOf(CategoryPerformance("IT", 50)),
            topPerformingJobs = listOf(JobAnalyticsMetrics(jobId = "job1", jobTitle = "Software Engineer", totalApplications = 5)),
            bottomPerformingJobs = emptyList(),
            companyAnalytics = CompanyProfileAnalytics(companyId = companyId, totalJobsPosted = 10, totalApplicationsReceived = 100, totalHires = 5)
        )

        // Stubbing
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, AnalyticsDashboard?) -> Unit>(1)
            callback(true, "Success", dashboardData)
            null
        }.`when`(analyticsRepo).getCompanyDashboard(eq(companyId), any())

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Int) -> Unit>(1)
            callback(50) // 50 followers
            null
        }.`when`(followRepo).getFollowersCount(eq(companyId), any())

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, List<ApplicationModel>?) -> Unit>(1)
            callback(true, "Success", emptyList())
            null
        }.`when`(applicationRepo).getApplicationsByCompany(eq(companyId), any())

        // ViewModel
        // Note: We use the main thread for ViewModel creation if possible, but here it's fine.
        // However, LiveData updates need to happen. Since we trigger loadCompanyDashboard in LaunchedEffect,
        // it will run on main thread (via viewModelScope).
        val viewModel = AnalyticsViewModel(app, analyticsRepo, followRepo, applicationRepo)

        // Set Content
        composeRule.setContent {
            AnalyticsScreen(viewModel = viewModel, companyId = companyId)
        }

        // Assertions
        // Verify Header
        composeRule.onNodeWithText("Analytics Dashboard").assertIsDisplayed()
        
        // Verify Metrics
        // Note: The UI might take a moment to settle, but composeRule handles idle waiting.
        // We check for values from our mocked data
        
        // Total Jobs: 10
        composeRule.onNodeWithText("10").assertIsDisplayed()
        
        // Total Applications: 100
        composeRule.onNodeWithText("100").assertIsDisplayed()
        
        // Hires: 5
        composeRule.onNodeWithText("5").assertIsDisplayed()
        
        // Followers: 50
        composeRule.onNodeWithText("50").assertIsDisplayed()
        
        // Check for Job Title in Top Performing Jobs
        composeRule.onNodeWithText("Software Engineer").assertIsDisplayed()
    }
}
