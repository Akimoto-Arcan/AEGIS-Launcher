package com.jarvis.launcher.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    @Json(name = "max_tokens") val maxTokens: Int,
    val temperature: Double
)

@JsonClass(generateAdapter = true)
data class ChatCompletionResponse(
    val id: String,
    val choices: List<Choice>
)

@JsonClass(generateAdapter = true)
data class Choice(
    val message: ChatMessage,
    @Json(name = "finish_reason") val finishReason: String?
)
