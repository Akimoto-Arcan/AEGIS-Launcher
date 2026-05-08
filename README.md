# A.E.G.I.S.

### **A**dvanced **E**lectronic **G**eneral **I**ntelligence **S**ystem

A sci-fi HUD launcher and AI voice assistant for Android.

---

## Install

1. Download **app-debug.apk** from the [latest release](https://github.com/Akimoto-Arcan/JarvisLauncher/releases/latest)
2. Open on your phone and tap **Install**
3. Go to **Settings → Apps → Default apps → Home app → AEGIS**

## Features

### HUD Launcher
- Animated arc reactor with rotating rings and scanning lines
- Up to 16 orbiting app icons you can drag around the ring
- HUD widgets: clock, date, battery arc, live weather
- Sci-fi app drawer with search (swipe up)
- 9 color themes

### AI Voice Assistant
- Say **"Aegis"** or tap the text to activate
- Conversation mode — stays open for follow-ups until dismissed
- 8 AI neural voices (Microsoft Edge TTS, free and unlimited)
- Powered by OpenRouter free LLM models
- Say "stop", "goodbye", or tap to dismiss

### Settings (tap reactor center)
- Color theme picker (Cyan, Blue, Red, Green, Purple, Orange, Gold, White, Pink)
- Voice selection (8 AI neural voices)
- Activation mode: **Always Listening** or **Tap Only**
- Orbiting app selector (up to 16)
- Temperature unit (°F / °C)
- OpenRouter API key

## Controls

| Action | What it does |
|--------|-------------|
| Say "Aegis" | Activate voice assistant (if always-listening is on) |
| Tap "A E G I S" text | Activate voice assistant |
| Tap reactor center | Open settings |
| Drag near orbit ring | Spin the app ring |
| Tap orbiting app | Launch app |
| Tap clock | Open alarm app |
| Swipe up | App drawer |
| Back / Home | Return to home |
| Say "stop" / "goodbye" | Dismiss assistant |

## Tech Stack

- Kotlin + Jetpack Compose
- Hilt, Retrofit + Moshi, Coroutines + Flow
- Microsoft Edge TTS (neural AI voices)
- OpenRouter API (free LLM models)
- Android SpeechRecognizer + Canvas API

## Requirements

- Android 12+ (API 31)
- Free OpenRouter API key ([openrouter.ai](https://openrouter.ai/))

## Build from Source

```bash
# Add your API key to local.properties
echo "OPENROUTER_API_KEY=sk-or-your-key" >> local.properties

./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```
