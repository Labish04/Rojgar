package com.example.rojgar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rojgar.model.FeedbackModel
import com.example.rojgar.repository.FeedbackRepo
import com.example.rojgar.repository.FeedbackRepoImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedbackViewModel(
    private val repository: FeedbackRepo = FeedbackRepoImpl()
) : ViewModel() {

    private val _feedbacks = MutableStateFlow<List<FeedbackModel>>(emptyList())
    val feedbacks: StateFlow<List<FeedbackModel>> = _feedbacks.asStateFlow()

    private val _sendingState = MutableStateFlow<SendingState>(SendingState.Idle)
    val sendingState: StateFlow<SendingState> = _sendingState.asStateFlow()

    sealed class SendingState {
        object Idle : SendingState()
        object Loading : SendingState()
        data class Success(val message: String) : SendingState()
        data class Error(val message: String) : SendingState()
    }

    fun sendFeedback(feedback: FeedbackModel) {
        viewModelScope.launch {
            _sendingState.value = SendingState.Loading
            repository.sendFeedback(feedback).fold(
                onSuccess = {
                    _sendingState.value = SendingState.Success("Feedback sent successfully!")
                },
                onFailure = { error ->
                    _sendingState.value = SendingState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }

    fun loadAllFeedbacks() {
        viewModelScope.launch {
            repository.getAllFeedbacks().collect { feedbackList ->
                _feedbacks.value = feedbackList
            }
        }
    }

    fun loadFeedbacksByCompany(companyId: String) {
        viewModelScope.launch {
            repository.getFeedbacksByCompany(companyId).collect { feedbackList ->
                _feedbacks.value = feedbackList
            }
        }
    }

    fun resetSendingState() {
        _sendingState.value = SendingState.Idle
    }
}