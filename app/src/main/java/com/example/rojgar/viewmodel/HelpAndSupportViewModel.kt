//package com.example.rojgar.viewmodel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.rojgar.model.HelpAndSupportModel
//import com.example.rojgar.model.Priority
//import com.example.rojgar.model.RequestStatus
//import com.example.rojgar.model.SupportCategory
//import com.example.rojgar.repository.HelpAndSupportRepo
//import com.example.rojgar.repository.HelpAndSupportRepoImpl
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//
//class HelpAndSupportViewModel(
//    private val repository: HelpAndSupportRepo = HelpAndSupportRepoImpl()
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow(HelpAndSupportUiState())
//    val uiState: StateFlow<HelpAndSupportUiState> = _uiState.asStateFlow()
//
//    private val _supportRequests = MutableStateFlow<List<HelpAndSupportModel>>(emptyList())
//    val supportRequests: StateFlow<List<HelpAndSupportModel>> = _supportRequests.asStateFlow()
//
//    private val _selectedRequest = MutableStateFlow<HelpAndSupportModel?>(null)
//    val selectedRequest: StateFlow<HelpAndSupportModel?> = _selectedRequest.asStateFlow()
//
//    // Update functions for form fields
//    fun updateCompanyName(name: String) {
//        _uiState.value = _uiState.value.copy(companyName = name)
//    }
//
//    fun updateCompanyEmail(email: String) {
//        _uiState.value = _uiState.value.copy(companyEmail = email)
//    }
//
//    fun updateSubject(subject: String) {
//        _uiState.value = _uiState.value.copy(subject = subject)
//    }
//
//    fun updateCategory(category: SupportCategory) {
//        _uiState.value = _uiState.value.copy(category = category)
//    }
//
//    fun updatePriority(priority: Priority) {
//        _uiState.value = _uiState.value.copy(priority = priority)
//    }
//
//    fun updateDescription(description: String) {
//        _uiState.value = _uiState.value.copy(description = description)
//    }
//
//    // Submit support request
//    fun submitSupportRequest() {
//        val state = _uiState.value
//
//        // Validation
//        if (state.companyName.isBlank()) {
//            _uiState.value = state.copy(
//                errorMessage = "Company name is required",
//                isLoading = false
//            )
//            return
//        }
//
//        if (state.companyEmail.isBlank()) {
//            _uiState.value = state.copy(
//                errorMessage = "Company email is required",
//                isLoading = false
//            )
//            return
//        }
//
//        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.companyEmail).matches()) {
//            _uiState.value = state.copy(
//                errorMessage = "Please enter a valid email address",
//                isLoading = false
//            )
//            return
//        }
//
//        if (state.subject.isBlank()) {
//            _uiState.value = state.copy(
//                errorMessage = "Subject is required",
//                isLoading = false
//            )
//            return
//        }
//
//        if (state.description.isBlank()) {
//            _uiState.value = state.copy(
//                errorMessage = "Description is required",
//                isLoading = false
//            )
//            return
//        }
//
//        _uiState.value = state.copy(isLoading = true, errorMessage = null)
//
//        viewModelScope.launch {
//            val request = HelpAndSupportModel(
//                companyName = state.companyName,
//                companyEmail = state.companyEmail,
//                subject = state.subject,
//                category = state.category,
//                priority = state.priority,
//                description = state.description
//            )
//
//            repository.submitSupportRequest(request)
//                .onSuccess { requestId ->
//                    _uiState.value = HelpAndSupportUiState(
//                        isLoading = false,
//                        successMessage = "Support request submitted successfully! Request ID: $requestId"
//                    )
//                }
//                .onFailure { exception ->
//                    _uiState.value = state.copy(
//                        isLoading = false,
//                        errorMessage = "Failed to submit request: ${exception.message}"
//                    )
//                }
//        }
//    }
//
//    // Load support requests for a company
//    fun loadSupportRequests(companyEmail: String) {
//        viewModelScope.launch {
//            try {
//                repository.getSupportRequests(companyEmail).collect { requests ->
//                    _supportRequests.value = requests
//                }
//            } catch (e: Exception) {
//                _uiState.value = _uiState.value.copy(
//                    errorMessage = "Failed to load requests: ${e.message}"
//                )
//            }
//        }
//    }
//
//    // Get all support requests (for admin view)
//    fun loadAllSupportRequests() {
//        repository.getAllSupportRequests { success, message, requests ->
//            if (success && requests != null) {
//                _supportRequests.value = requests
//            } else {
//                _uiState.value = _uiState.value.copy(
//                    errorMessage = message
//                )
//            }
//        }
//    }
//
//    // Get support request by ID
//    fun loadSupportRequestById(requestId: String) {
//        repository.getSupportRequestById(requestId) { success, message, request ->
//            if (success && request != null) {
//                _selectedRequest.value = request
//            } else {
//                _uiState.value = _uiState.value.copy(
//                    errorMessage = message
//                )
//            }
//        }
//    }
//
//    // Update request status
//    fun updateRequestStatus(requestId: String, newStatus: RequestStatus) {
//        viewModelScope.launch {
//            repository.updateRequestStatus(requestId, newStatus.name)
//                .onSuccess {
//                    _uiState.value = _uiState.value.copy(
//                        successMessage = "Status updated successfully"
//                    )
//                }
//                .onFailure { exception ->
//                    _uiState.value = _uiState.value.copy(
//                        errorMessage = "Failed to update status: ${exception.message}"
//                    )
//                }
//        }
//    }
//
//    // Delete support request
//    fun deleteSupportRequest(requestId: String) {
//        viewModelScope.launch {
//            repository.deleteSupportRequest(requestId)
//                .onSuccess {
//                    _uiState.value = _uiState.value.copy(
//                        successMessage = "Request deleted successfully"
//                    )
//                }
//                .onFailure { exception ->
//                    _uiState.value = _uiState.value.copy(
//                        errorMessage = "Failed to delete request: ${exception.message}"
//                    )
//                }
//        }
//    }
//
//    // Filter requests by status
//    fun filterByStatus(status: RequestStatus) {
//        repository.getSupportRequestsByStatus(status) { success, message, requests ->
//            if (success && requests != null) {
//                _supportRequests.value = requests
//            } else {
//                _uiState.value = _uiState.value.copy(
//                    errorMessage = message
//                )
//            }
//        }
//    }
//
//    // Filter requests by category
//    fun filterByCategory(category: SupportCategory) {
//        repository.getSupportRequestsByCategory(category.name) { success, message, requests ->
//            if (success && requests != null) {
//                _supportRequests.value = requests
//            } else {
//                _uiState.value = _uiState.value.copy(
//                    errorMessage = message
//                )
//            }
//        }
//    }
//
//    // Clear messages
//    fun clearMessages() {
//        _uiState.value = _uiState.value.copy(
//            successMessage = null,
//            errorMessage = null
//        )
//    }
//
//    // Reset form
//    fun resetForm() {
//        _uiState.value = HelpAndSupportUiState()
//    }
//}
//
//data class HelpAndSupportUiState(
//    val companyName: String = "",
//    val companyEmail: String = "",
//    val subject: String = "",
//    val category: SupportCategory = SupportCategory.GENERAL,
//    val priority: Priority = Priority.MEDIUM,
//    val description: String = "",
//    val isLoading: Boolean = false,
//    val successMessage: String? = null,
//    val errorMessage: String? = null
//)