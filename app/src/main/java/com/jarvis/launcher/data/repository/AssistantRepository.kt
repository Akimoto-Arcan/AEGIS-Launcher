package com.jarvis.launcher.data.repository

import com.jarvis.launcher.data.model.ChatCompletionRequest
import com.jarvis.launcher.data.model.ChatMessage
import com.jarvis.launcher.data.remote.OpenRouterApi
import com.jarvis.launcher.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistantRepository @Inject constructor(
    private val api: OpenRouterApi
) {

    private val conversationHistory = mutableListOf(
        ChatMessage(role = "system", content = Constants.JARVIS_SYSTEM_PROMPT)
    )

    suspend fun chat(userMessage: String): String {
        return try {
            conversationHistory.add(ChatMessage(role = "user", content = userMessage))
            trimHistory()

            val request = ChatCompletionRequest(
                model = Constants.OPENROUTER_MODEL,
                messages = conversationHistory.toList(),
                maxTokens = Constants.MAX_TOKENS,
                temperature = Constants.TEMPERATURE
            )

            val response = api.chatCompletion(request)
            val assistantMessage = response.choices.firstOrNull()?.message?.content
                ?: "I'm afraid I have no response at this time, sir."

            conversationHistory.add(ChatMessage(role = "assistant", content = assistantMessage))
            trimHistory()

            assistantMessage
        } catch (e: Exception) {
            "I do apologize, sir, but I'm experiencing a temporary disruption in my systems. Perhaps we could try again in a moment."
        }
    }

    private fun trimHistory() {
        val systemMessage = conversationHistory.first()
        if (conversationHistory.size > 21) {
            val trimmed = mutableListOf(systemMessage)
            trimmed.addAll(conversationHistory.takeLast(20))
            conversationHistory.clear()
            conversationHistory.addAll(trimmed)
        }
    }
}
