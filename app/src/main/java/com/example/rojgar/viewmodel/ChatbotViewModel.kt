package com.example.rojgar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rojgar.model.ChatbotMessage
import com.example.rojgar.model.ChatbotState
import com.example.rojgar.repository.ChatbotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for managing chatbot state and interactions
 */
class ChatbotViewModel(
    private val repository: ChatbotRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatbotState())
    val state: StateFlow<ChatbotState> = _state.asStateFlow()

    init {
        // Add welcome message
        addMessage(
            ChatbotMessage(
                text = "Hello! I'm your AI assistant. How can I help you today?",
                isUser = false
            )
        )
    }

    /**
     * Sends a user message and gets AI response
     */
    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // Add user message
        val userMessage = ChatbotMessage(text = text.trim(), isUser = true)
        addMessage(userMessage)

        // Set loading state
        _state.update { it.copy(isLoading = true, error = null) }

        // Get AI response
        viewModelScope.launch {
            repository.sendMessage(
                message = text,
                onSuccess = { response ->
                    val aiMessage = ChatbotMessage(text = response, isUser = false)
                    addMessage(aiMessage)
                    _state.update { it.copy(isLoading = false) }
                },
                onError = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error
                        )
                    }
                }
            )
        }
    }

    /**
     * Adds a message to the conversation
     */
    private fun addMessage(message: ChatbotMessage) {
        _state.update { currentState ->
            currentState.copy(
                messages = currentState.messages + message
            )
        }
    }

    /**
     * Clears all messages and resets to initial state
     */
    fun clearChat() {
        _state.value = ChatbotState()
        addMessage(
            ChatbotMessage(
                text = "Hello! I'm your AI assistant. How can I help you today?",
                isUser = false
            )
        )
    }

    /**
     * Clears any error message
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}