package com.example.rojgar.repository

import com.example.rojgar.model.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Implementation of ChatbotRepository using Gemini API
 */
class ChatbotRepositoryImpl(
    private val apiKey: String
) : ChatbotRepository {

    private val gson = Gson()
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

    override suspend fun sendMessage(
        message: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("$baseUrl?key=$apiKey")
            connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                doInput = true
                connectTimeout = 30000
                readTimeout = 30000
            }

            // Build request body
            val requestBody = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(text = message))
                    )
                )
            )

            // Send request
            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(gson.toJson(requestBody))
            writer.flush()
            writer.close()

            // Read response
            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                // Parse response
                val geminiResponse = gson.fromJson(response.toString(), GeminiResponse::class.java)
                val aiMessage = geminiResponse.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text

                withContext(Dispatchers.Main) {
                    if (aiMessage != null && aiMessage.isNotBlank()) {
                        onSuccess(aiMessage)
                    } else {
                        onError("No response from AI. Response: ${response.toString().take(100)}")
                    }
                }
            } else {
                val errorReader = BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream))
                val errorResponse = StringBuilder()
                var line: String?
                while (errorReader.readLine().also { line = it } != null) {
                    errorResponse.append(line)
                }
                errorReader.close()

                withContext(Dispatchers.Main) {
                    val errorMsg = errorResponse.toString()
                    // Extract just the error message if it's JSON
                    val cleanError = if (errorMsg.contains("message")) {
                        try {
                            val jsonError = gson.fromJson(errorMsg, com.google.gson.JsonObject::class.java)
                            jsonError.getAsJsonObject("error")?.get("message")?.asString ?: errorMsg.take(200)
                        } catch (e: Exception) {
                            errorMsg.take(200)
                        }
                    } else {
                        errorMsg.take(200)
                    }
                    onError("API Error ($responseCode): $cleanError")
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("Network error: ${e.message ?: "Unknown error"}")
            }
        } finally {
            connection?.disconnect()
        }
    }
}