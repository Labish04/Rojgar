package com.example.rojgar.repository

import com.example.rojgar.model.AnalyticsDashboard
import com.example.rojgar.model.JobAnalyticsMetrics
import com.example.rojgar.model.ConversionMetrics
import com.example.rojgar.model.CategoryPerformance
import com.example.rojgar.model.CompanyProfileAnalytics

interface AnalyticsRepo {

    fun getCompanyDashboard(
        companyId: String,
        callback: (Boolean, String, AnalyticsDashboard?) -> Unit
    )

    fun getJobMetrics(
        companyId: String,
        callback: (Boolean, String, List<JobAnalyticsMetrics>?) -> Unit
    )

    fun getConversionMetrics(
        companyId: String,
        callback: (Boolean, String, ConversionMetrics?) -> Unit
    )

    fun getCategoryPerformance(
        companyId: String,
        callback: (Boolean, String, List<CategoryPerformance>?) -> Unit
    )

    fun getCompanyProfileAnalytics(
        companyId: String,
        callback: (Boolean, String, CompanyProfileAnalytics?) -> Unit
    )

    fun getTopPerformingJobs(
        companyId: String,
        limit: Int = 5,
        callback: (Boolean, String, List<JobAnalyticsMetrics>?) -> Unit
    )

    fun trackJobView(
        jobId: String,
        callback: (Boolean, String) -> Unit
    )

    fun trackJobSave(
        jobId: String,
        callback: (Boolean, String) -> Unit
    )

    fun trackProfileView(
        companyId: String,
        callback: (Boolean, String) -> Unit
    )

    fun trackApplicationStatus(
        jobId: String,
        applicationId: String,
        status: String, // "applied", "shortlisted", "hired", "rejected"
        callback: (Boolean, String) -> Unit
    )
}
