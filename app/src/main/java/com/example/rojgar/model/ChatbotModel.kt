package com.example.rojgar.model

/**
 * Represents a single chat message in the conversation
 */
data class ChatbotMessage(
    val text: String = "",
    val isUser: Boolean = false,
    val id: String = System.currentTimeMillis().toString(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Represents the conversation state
 */
data class ChatbotState(
    val messages: List<ChatbotMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Internal data classes for Gemini API communication
 * (Kept in model file to avoid creating separate DTO files)
 */

// Request structures
internal data class GeminiRequest(
    val contents: List<Content>
)

internal data class Content(
    val parts: List<Part>
)

internal data class Part(
    val text: String
)

// Response structures
internal data class GeminiResponse(
    val candidates: List<Candidate>?
)

internal data class Candidate(
    val content: ContentResponse?
)

internal data class ContentResponse(
    val parts: List<PartResponse>?
)

internal data class PartResponse(
    val text: String?
)