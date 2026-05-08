package com.jarvis.launcher.util

object Constants {
    const val OPENROUTER_BASE_URL = "https://openrouter.ai/"
    const val OPENROUTER_MODEL = "openrouter/auto"
    const val MAX_TOKENS = 256
    const val TEMPERATURE = 0.7

    const val WAKE_WORD_THRESHOLD = 0.5f
    const val NOTIFICATION_CHANNEL_ID = "aegis_wake_word"
    const val NOTIFICATION_ID = 1001

    const val AEGIS_SYSTEM_PROMPT = """You are AEGIS, the Advanced Electronic General Intelligence System. You are a sophisticated AI assistant with a calm, professional demeanor. You speak concisely and clearly. Keep responses under 3 sentences since they will be spoken aloud. Address the user as 'sir' when appropriate. You are running on their mobile device as a launcher and voice assistant."""
}
