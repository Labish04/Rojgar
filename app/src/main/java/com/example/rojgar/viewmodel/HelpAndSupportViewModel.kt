package com.example.rojgar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rojgar.model.Priority
import com.example.rojgar.model.SupportCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class HelpAndSupportUiState(
    val subject: String = "",
    val category: SupportCategory = SupportCategory.TECHNICAL,
    val priority: Priority = Priority.MEDIUM,
    val description: String = "",
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class HelpAndSupportViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HelpAndSupportUiState())
    val uiState: StateFlow<HelpAndSupportUiState> = _uiState.asStateFlow()

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
        val currentState = _uiState.value

        // Validation - only check required fields
        if (currentState.subject.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Please enter a subject"
            )
            return
        }

        if (currentState.description.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Please provide a description"
            )
            return
        }

        // Submit the request
        viewModelScope.launch {
            _uiState.value = currentState.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            try {
                // Simulate API call
                delay(1500)



                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Support request submitted successfully! We'll get back to you soon.",
                    // Clear form after successful submission
                    subject = "",
                    description = "",
                    category = SupportCategory.TECHNICAL,
                    priority = Priority.MEDIUM
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to submit request. Please try again."
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
}