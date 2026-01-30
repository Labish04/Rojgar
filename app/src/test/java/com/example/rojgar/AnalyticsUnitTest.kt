package com.example.rojgar

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.rojgar.model.*
import com.example.rojgar.repository.AnalyticsRepo
import com.example.rojgar.repository.ApplicationRepo
import com.example.rojgar.repository.FollowRepo
import com.example.rojgar.viewmodel.AnalyticsViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.*

class AnalyticsUnitTest {

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    @Test
    fun loadCompanyDashboard_success_test() {
        // Mocks
        val app = mock<Application>()
        val analyticsRepo = mock<AnalyticsRepo>()
        val followRepo = mock<FollowRepo>()
        val applicationRepo = mock<ApplicationRepo>()
        
        // Data
        val companyId = "company123"
        val dashboardData = AnalyticsDashboard(
            jobMetrics = listOf(JobAnalyticsMetrics(jobId = "job1", jobTitle = "Developer")),
            conversionMetrics = ConversionMetrics(totalApplications = 10, totalHired = 2),
            categoryPerformance = emptyList(),
            topPerformingJobs = emptyList(),
            bottomPerformingJobs = emptyList(),
            companyAnalytics = CompanyProfileAnalytics(companyId = companyId)
        )
        val applications = listOf(
            ApplicationModel(applicationId = "app1", jobSeekerId = "seeker1"),
            ApplicationModel(applicationId = "app2", jobSeekerId = "seeker2"),
            ApplicationModel(applicationId = "app3", jobSeekerId = "seeker1") // Duplicate seeker
        )

        // Stubbing
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, AnalyticsDashboard?) -> Unit>(1)
            callback(true, "Success", dashboardData)
            null
        }.`when`(analyticsRepo).getCompanyDashboard(eq(companyId), any())

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Int) -> Unit>(1)
            callback(100) // 100 followers
            null
        }.`when`(followRepo).getFollowersCount(eq(companyId), any())

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, List<ApplicationModel>?) -> Unit>(1)
            callback(true, "Success", applications)
            null
        }.`when`(applicationRepo).getApplicationsByCompany(eq(companyId), any())

        // ViewModel
        val viewModel = AnalyticsViewModel(app, analyticsRepo, followRepo, applicationRepo)

        // Action
        viewModel.loadCompanyDashboard(companyId)

        // Assertions
        assertEquals(dashboardData, viewModel.dashboard.value)
        assertEquals(100, viewModel.followersCount.value)
        assertEquals(2, viewModel.applicationCount.value) // 2 unique seekers
        assertEquals(false, viewModel.loading.value)
        assertEquals("", viewModel.errorMessage.value)
        
        // Verify Interactions
        verify(analyticsRepo).getCompanyDashboard(eq(companyId), any())
        verify(followRepo).getFollowersCount(eq(companyId), any())
        verify(applicationRepo).getApplicationsByCompany(eq(companyId), any())
    }

    @Test
    fun loadCompanyDashboard_failure_test() {
        // Mocks
        val app = mock<Application>()
        val analyticsRepo = mock<AnalyticsRepo>()
        val followRepo = mock<FollowRepo>()
        val applicationRepo = mock<ApplicationRepo>()
        
        val companyId = "company123"
        val errorMessage = "Network Error"

        // Stubbing failure
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, AnalyticsDashboard?) -> Unit>(1)
            callback(false, errorMessage, null)
            null
        }.`when`(analyticsRepo).getCompanyDashboard(eq(companyId), any())

        // ViewModel
        val viewModel = AnalyticsViewModel(app, analyticsRepo, followRepo, applicationRepo)

        // Action
        viewModel.loadCompanyDashboard(companyId)

        // Assertions
        assertEquals(errorMessage, viewModel.errorMessage.value)
        assertEquals(false, viewModel.loading.value)
    }
}
