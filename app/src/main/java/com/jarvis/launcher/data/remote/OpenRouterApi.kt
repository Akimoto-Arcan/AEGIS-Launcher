package com.jarvis.launcher.data.remote

import com.jarvis.launcher.data.model.ChatCompletionRequest
import com.jarvis.launcher.data.model.ChatCompletionResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenRouterApi {

    @POST("api/v1/chat/completions")
    suspend fun chatCompletion(@Body request: ChatCompletionRequest): ChatCompletionResponse
}
