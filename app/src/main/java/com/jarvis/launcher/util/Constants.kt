package com.jarvis.launcher.util

object Constants {
    const val OPENROUTER_BASE_URL = "https://openrouter.ai/"
    const val OPENROUTER_MODEL = "openrouter/auto"
    const val MAX_TOKENS = 256
    const val TEMPERATURE = 0.7

    const val WAKE_WORD_THRESHOLD = 0.5f
    const val NOTIFICATION_CHANNEL_ID = "jarvis_wake_word"
    const val NOTIFICATION_ID = 1001

    const val JARVIS_SYSTEM_PROMPT = """You are JARVIS, an advanced AI assistant inspired by the AI from Iron Man.
You speak in a formal British English tone. You are concise, witty, and helpful.
Keep responses under 3 sentences since they will be spoken aloud.
Address the user as "sir" when appropriate.
You are running on their mobile device as a launcher and voice assistant."""
}
