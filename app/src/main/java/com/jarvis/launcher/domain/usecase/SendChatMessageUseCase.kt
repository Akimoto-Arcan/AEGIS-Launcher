package com.jarvis.launcher.domain.usecase

import com.jarvis.launcher.data.repository.AssistantRepository
import javax.inject.Inject

class SendChatMessageUseCase @Inject constructor(
    private val assistantRepository: AssistantRepository
) {
    suspend operator fun invoke(message: String): String = assistantRepository.chat(message)
}
