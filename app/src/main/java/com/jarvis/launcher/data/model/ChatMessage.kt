package com.jarvis.launcher.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatMessage(
    val role: String,
    val content: String
)
