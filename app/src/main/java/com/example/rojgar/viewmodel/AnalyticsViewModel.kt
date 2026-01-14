package com.example.rojgar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rojgar.model.AnalyticsDashboard
import com.example.rojgar.model.JobAnalyticsMetrics
import com.example.rojgar.model.ConversionMetrics
import com.example.rojgar.model.CategoryPerformance
import com.example.rojgar.model.CompanyProfileAnalytics
import com.example.rojgar.repository.AnalyticsRepoImpl
import com.example.rojgar.repository.FollowRepoImpl
import com.example.rojgar.repository.ApplicationRepoImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AnalyticsViewModel : ViewModel() {

    private val repo: AnalyticsRepoImpl = AnalyticsRepoImpl()
    private val followRepo: FollowRepoImpl = FollowRepoImpl()
    private val applicationRepo: ApplicationRepoImpl = ApplicationRepoImpl()
    private var currentCompanyId: String = ""
    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // Loading State
    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    // Dashboard
    private val _dashboard = MutableLiveData<AnalyticsDashboard>()
    val dashboard: LiveData<AnalyticsDashboard> = _dashboard

    // Job Metrics
    private val _jobMetrics = MutableLiveData<List<JobAnalyticsMetrics>>(emptyList())
    val jobMetrics: LiveData<List<JobAnalyticsMetrics>> = _jobMetrics

    // Conversion Metrics
    private val _conversionMetrics = MutableLiveData<ConversionMetrics>()
    val conversionMetrics: LiveData<ConversionMetrics> = _conversionMetrics

    // Category Performance
    private val _categoryPerformance = MutableLiveData<List<CategoryPerformance>>(emptyList())
    val categoryPerformance: LiveData<List<CategoryPerformance>> = _categoryPerformance

    // Company Profile Analytics
    private val _companyProfile = MutableLiveData<CompanyProfileAnalytics>()
    val companyProfile: LiveData<CompanyProfileAnalytics> = _companyProfile

    // Followers Count
    private val _followersCount = MutableLiveData<Int>(0)
    val followersCount: LiveData<Int> = _followersCount

    // Application Count from direct fetch
    private val _applicationCount = MutableLiveData<Int>(0)
    val applicationCount: LiveData<Int> = _applicationCount

    // Top Performing Jobs
    private val _topJobs = MutableLiveData<List<JobAnalyticsMetrics>>(emptyList())
    val topJobs: LiveData<List<JobAnalyticsMetrics>> = _topJobs

    // Bottom Performing Jobs
    private val _bottomJobs = MutableLiveData<List<JobAnalyticsMetrics>>(emptyList())
    val bottomJobs: LiveData<List<JobAnalyticsMetrics>> = _bottomJobs

    // Error Messages
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun loadCompanyDashboard(companyId: String) {
        this.currentCompanyId = companyId
        _loading.postValue(true)
        _errorMessage.postValue("")
        
        repo.getCompanyDashboard(companyId) { success, message, analyticsData ->
            _loading.postValue(false)
            if (success && analyticsData != null) {
                _dashboard.postValue(analyticsData)
                _jobMetrics.postValue(analyticsData.jobMetrics)
                _conversionMetrics.postValue(analyticsData.conversionMetrics)
                _categoryPerformance.postValue(analyticsData.categoryPerformance)
                _topJobs.postValue(analyticsData.topPerformingJobs)
                _bottomJobs.postValue(analyticsData.bottomPerformingJobs)
                _companyProfile.postValue(analyticsData.companyAnalytics)
                _errorMessage.postValue("")
            } else {
                _errorMessage.postValue(message ?: "Failed to load analytics")
            }
        }
        
        // Fetch actual followers count from FollowRepo
        loadFollowersCount(companyId)
        
        // Fetch application count from ApplicationRepo
        loadApplicationCount(companyId)
    }

    fun loadFollowersCount(companyId: String) {
        followRepo.getFollowersCount(companyId) { count ->
            _followersCount.postValue(count)
            // Update the company profile with actual followers count
            _companyProfile.value?.let { profile ->
                val updatedProfile = profile.copy(followers = count)
                _companyProfile.postValue(updatedProfile)
            }
        }
    }

    fun loadApplicationCount(companyId: String) {
        applicationRepo.getApplicationsByCompany(companyId) { success, message, applications ->
            if (success && applications != null) {
                // Count unique jobseekers (some jobseekers may apply to multiple jobs)
                val uniqueApplicantCount = applications
                    .mapNotNull { it.jobSeekerId }
                    .filter { it.isNotEmpty() }
                    .toSet()
                    .size

                _applicationCount.postValue(uniqueApplicantCount)

                // Update the company profile with actual unique applicant count
                _companyProfile.value?.let { profile ->
                    val updatedProfile = profile.copy(totalApplicationsReceived = uniqueApplicantCount)
                    _companyProfile.postValue(updatedProfile)
                }
            }
        }
    }

    fun refreshData() {
        if (currentCompanyId.isNotEmpty()) {
            loadCompanyDashboard(currentCompanyId)
        }
    }

    fun startAutoRefresh(intervalMs: Long = 5000) {
        viewModelScope.launch {
            while (true) {
                delay(intervalMs)
                refreshData()
            }
        }
    }

    fun loadJobMetrics(companyId: String) {
        _loading.postValue(true)
        repo.getJobMetrics(companyId) { success, message, metrics ->
            _loading.postValue(false)
            if (success && metrics != null) {
                _jobMetrics.postValue(metrics)
            } else {
                _errorMessage.postValue(message)
            }
        }
    }

    fun loadConversionMetrics(companyId: String) {
        _loading.postValue(true)
        repo.getConversionMetrics(companyId) { success, message, metrics ->
            _loading.postValue(false)
            if (success && metrics != null) {
                _conversionMetrics.postValue(metrics)
            } else {
                _errorMessage.postValue(message)
            }
        }
    }

    fun loadCategoryPerformance(companyId: String) {
        _loading.postValue(true)
        repo.getCategoryPerformance(companyId) { success, message, categories ->
            _loading.postValue(false)
            if (success && categories != null) {
                _categoryPerformance.postValue(categories)
            } else {
                _errorMessage.postValue(message)
            }
        }
    }

    fun loadCompanyProfileAnalytics(companyId: String) {
        _loading.postValue(true)
        repo.getCompanyProfileAnalytics(companyId) { success, message, analytics ->
            _loading.postValue(false)
            if (success && analytics != null) {
                _companyProfile.postValue(analytics)
            } else {
                _errorMessage.postValue(message)
            }
        }
    }

    fun loadTopPerformingJobs(companyId: String, limit: Int = 5) {
        repo.getTopPerformingJobs(companyId, limit) { success, message, jobs ->
            if (success && jobs != null) {
                _topJobs.postValue(jobs)
            } else {
                _errorMessage.postValue(message)
            }
        }
    }

    fun trackJobView(jobId: String) {
        repo.trackJobView(jobId) { success, message ->
            if (!success) {
                _errorMessage.postValue(message)
            }
        }
    }

    fun trackJobSave(jobId: String) {
        repo.trackJobSave(jobId) { success, message ->
            if (!success) {
                _errorMessage.postValue(message)
            }
        }
    }

    fun trackProfileView(companyId: String) {
        repo.trackProfileView(companyId) { success, message ->
            if (!success) {
                _errorMessage.postValue(message)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
