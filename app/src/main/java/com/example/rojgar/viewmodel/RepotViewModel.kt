package com.example.rojgar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rojgar.model.ReportModel
import com.example.rojgar.repository.ReportRepoImpl
import kotlinx.coroutines.launch

class ReportsViewModel : ViewModel() {

    private val repository = ReportRepoImpl()

    // LiveData for all reports
    private val _allReports = MutableLiveData<List<ReportModel>>()
    val allReports: LiveData<List<ReportModel>> = _allReports

    // LiveData for reports by company
    private val _companyReports = MutableLiveData<List<ReportModel>>()
    val companyReports: LiveData<List<ReportModel>> = _companyReports

    // LiveData for reports by reporter
    private val _reporterReports = MutableLiveData<List<ReportModel>>()
    val reporterReports: LiveData<List<ReportModel>> = _reporterReports

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Load all reports (for admin)
    fun loadAllReports() {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getAllReports { success, message, reports ->
                _isLoading.value = false
                if (success && reports != null) {
                    _allReports.value = reports
                } else {
                    _errorMessage.value = message
                }
            }
        }
    }

    // Load reports for a specific company
    fun loadReportsByCompany(companyId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getReportsByCompanyId(companyId) { success, message, reports ->
                _isLoading.value = false
                if (success && reports != null) {
                    _companyReports.value = reports
                } else {
                    _errorMessage.value = message
                }
            }
        }
    }

    // Load reports by a specific reporter
    fun loadReportsByReporter(reporterId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getReportsByReporterId(reporterId) { success, message, reports ->
                _isLoading.value = false
                if (success && reports != null) {
                    _reporterReports.value = reports
                } else {
                    _errorMessage.value = message
                }
            }
        }
    }

    // Submit a new report
    fun submitReport(report: ReportModel, onComplete: (Boolean, String) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.submitReport(report) { success, message ->
                _isLoading.value = false
                if (success) {
                    // Refresh reports if needed
                    loadAllReports()
                }
                onComplete(success, message)
            }
        }
    }

    // Update report status (for admin)
    fun updateReportStatus(reportId: String, status: String, adminNotes: String, resolvedBy: String = "Admin") {
        _isLoading.value = true
        viewModelScope.launch {
            repository.updateReportStatus(reportId, status, adminNotes, resolvedBy) { success, message ->
                _isLoading.value = false
                if (success) {
                    // Refresh all reports
                    loadAllReports()
                } else {
                    _errorMessage.value = message
                }
            }
        }
    }

    // Get report statistics
    fun getReportStatistics(): Map<String, Int> {
        val currentReports = _allReports.value ?: emptyList()
        return mapOf(
            "total" to currentReports.size,
            "pending" to currentReports.count { it.status == "pending" },
            "reviewing" to currentReports.count { it.status == "reviewing" },
            "resolved" to currentReports.count { it.status == "resolved" },
            "dismissed" to currentReports.count { it.status == "dismissed" }
        )
    }

    // Filter reports by status
    fun filterReportsByStatus(status: String): List<ReportModel> {
        return _allReports.value?.filter { it.status == status } ?: emptyList()
    }

    // Search reports by company name or reporter name
    fun searchReports(query: String): List<ReportModel> {
        return _allReports.value?.filter { report ->
            report.reportedCompanyName.contains(query, ignoreCase = true) ||
                    report.reporterName.contains(query, ignoreCase = true) ||
                    report.reportCategory.contains(query, ignoreCase = true) ||
                    report.reportReason.contains(query, ignoreCase = true)
        } ?: emptyList()
    }

    // Clear error message
    fun clearErrorMessage() {
        _errorMessage.value = ""
    }
}