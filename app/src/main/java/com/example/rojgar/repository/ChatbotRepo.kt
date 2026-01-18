package com.example.rojgar.repository

/**
 * Repository interface for chatbot operations
 */
interface ChatbotRepository {
    /**
     * Sends a message to Gemini API and returns the response
     * @param message User's message text
     * @param onSuccess Callback with the AI response
     * @param onError Callback with error message
     */
    suspend fun sendMessage(
        message: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    )
}