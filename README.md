# JARVIS Launcher

A sci-fi Android launcher inspired by Iron Man's JARVIS AI, built for Samsung Galaxy S24+.

## Features

### Iron Man HUD Launcher
- Animated arc reactor centerpiece with rotating rings and scanning lines
- HUD-style widgets: clock, date, battery gauge, weather
- Sci-fi app drawer with search and 4-column grid
- Edge-to-edge holographic design with cyan/blue color scheme

### AI Voice Assistant
- **Wake phrase**: "Jarvis" — detected via continuous audio monitoring
- **Speech Recognition**: On-device Android SpeechRecognizer
- **LLM Backend**: OpenRouter free models (OpenAI-compatible API)
- **Voice Output**: British English TTS with JARVIS persona
- **Floating Overlay**: HUD-themed overlay with visualizers and transcript display

## Install (Download APK)

1. Go to the [latest release](https://github.com/Akimoto-Arcan/JarvisLauncher/releases/latest)
2. Download **app-debug.apk** to your phone
3. Open the file and tap **Install** (allow unknown sources if prompted)
4. Go to **Settings → Apps → Default apps → Home app → JARVIS**
5. Press home — you're in the JARVIS launcher
6. Grant microphone and overlay permissions when prompted

## Build from Source

1. Get a free API key from [OpenRouter](https://openrouter.ai/)
2. Add to `local.properties`:
   ```
   OPENROUTER_API_KEY=sk-or-your-key-here
   ```
3. Build:
   ```bash
   ./gradlew assembleDebug
   ```
4. Install:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```
5. Set as default launcher: **Settings → Apps → Default apps → Home → JARVIS**

## Tech Stack

- Kotlin + Jetpack Compose
- Hilt (dependency injection)
- Retrofit + Moshi (networking)
- Canvas API (HUD animations)
- Android SpeechRecognizer + TextToSpeech
- Coroutines + Flow

## Requirements

- Android 12+ (API 31)
- Compile SDK 35
- Java 17

## Permissions

- `RECORD_AUDIO` — wake word detection and speech recognition
- `INTERNET` — LLM API calls
- `SYSTEM_ALERT_WINDOW` — floating voice assistant overlay
- `QUERY_ALL_PACKAGES` — app drawer
- `FOREGROUND_SERVICE` — background wake word listening
