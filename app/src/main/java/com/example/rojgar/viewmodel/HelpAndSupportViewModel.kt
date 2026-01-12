package com.example.rojgar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rojgar.model.HelpAndSupportModel
import com.example.rojgar.model.Priority
import com.example.rojgar.model.SupportCategory
import com.example.rojgar.repository.HelpAndSupportRepo
import com.example.rojgar.repository.HelpAndSupportRepoImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HelpAndSupportViewModel(
    private val repository: HelpAndSupportRepo = HelpAndSupportRepoImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HelpAndSupportUiState())
    val uiState: StateFlow<HelpAndSupportUiState> = _uiState.asStateFlow()

    private val _supportRequests = MutableStateFlow<List<HelpAndSupportModel>>(emptyList())
    val supportRequests: StateFlow<List<HelpAndSupportModel>> = _supportRequests.asStateFlow()

    fun updateCompanyName(name: String) {
        _uiState.value = _uiState.value.copy(companyName = name)
    }

    fun updateCompanyEmail(email: String) {
        _uiState.value = _uiState.value.copy(companyEmail = email)
    }

    fun updateSubject(subject: String) {
        _uiState.value = _uiState.value.copy(subject = subject)
    }

    fun updateCategory(category: SupportCategory) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun updatePriority(priority: Priority) {
        _uiState.value = _uiState.value.copy(priority = priority)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun submitSupportRequest() {
        val state = _uiState.value

        // Validation
        if (state.companyName.isBlank() || state.companyEmail.isBlank() ||
            state.subject.isBlank() || state.description.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Please fill in all required fields",
                isLoading = false
            )
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.companyEmail).matches()) {
            _uiState.value = state.copy(
                errorMessage = "Please enter a valid email address",
                isLoading = false
            )
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val request = HelpAndSupportModel(
                companyName = state.companyName,
                companyEmail = state.companyEmail,
                subject = state.subject,
                category = state.category,
                priority = state.priority,
                description = state.description
            )

            repository.submitSupportRequest(request)
                .onSuccess { requestId ->
                    _uiState.value = HelpAndSupportUiState(
                        isLoading = false,
                        successMessage = "Support request submitted successfully! Request ID: $requestId"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = state.copy(
                        isLoading = false,
                        errorMessage = "Failed to submit request: ${exception.message}"
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }

    fun loadSupportRequests(companyEmail: String) {
        viewModelScope.launch {
            repository.getSupportRequests(companyEmail).collect { requests ->
                _supportRequests.value = requests
            }
        }
    }
}

data class HelpAndSupportUiState(
    val companyName: String = "",
    val companyEmail: String = "",
    val subject: String = "",
    val category: SupportCategory = SupportCategory.GENERAL,
    val priority: Priority = Priority.MEDIUM,
    val description: String = "",
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)