package com.jarvis.launcher.domain.voice

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class EdgeTtsEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private var mediaPlayer: MediaPlayer? = null

    suspend fun speak(text: String, voice: String = "en-US-AndrewMultilingualNeural"): Boolean {
        try {
            Log.d("EdgeTTS", "Starting synthesis: voice=$voice text=$text")
            val audioFile = withContext(Dispatchers.IO) { synthesize(text, voice) }

            if (audioFile == null) {
                Log.e("EdgeTTS", "Synthesis returned null — no audio data received")
                return false
            }

            Log.d("EdgeTTS", "Synthesis complete, file size=${audioFile.length()} bytes")

            if (audioFile.length() < 100) {
                Log.e("EdgeTTS", "Audio file too small (${audioFile.length()} bytes), likely empty")
                audioFile.delete()
                return false
            }

            withContext(Dispatchers.Main) { playAudio(audioFile) }
            audioFile.delete()
            return true
        } catch (e: Exception) {
            Log.e("EdgeTTS", "Failed to speak", e)
            return false
        }
    }

    private suspend fun synthesize(text: String, voice: String): File? =
        suspendCancellableCoroutine { cont ->
            val connectionId = UUID.randomUUID().toString().replace("-", "")
            val requestId = UUID.randomUUID().toString().replace("-", "")

            val url = "$WSS_URL?TrustedClientToken=$TOKEN&ConnectionId=$connectionId"
            val request = Request.Builder()
                .url(url)
                .addHeader("Origin", "chrome-extension://jdiccldimpdaibmpdkjnbmckianbfold")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()

            val audioFile = File(context.cacheDir, "tts_$requestId.mp3")
            val outputStream = FileOutputStream(audioFile)
            var audioStarted = false
            var resumed = false

            Log.d("EdgeTTS", "Connecting WebSocket...")

            val ws = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d("EdgeTTS", "WebSocket connected")

                    val configMsg = "Content-Type:application/json; charset=utf-8\r\n" +
                            "Path:speech.config\r\n\r\n" +
                            """{"context":{"synthesis":{"audio":{"metadataoptions":{""" +
                            """"sentenceBoundaryEnabled":"false","wordBoundaryEnabled":"false"},""" +
                            """"outputFormat":"audio-24khz-96kbitrate-mono-mp3"}}}}"""
                    webSocket.send(configMsg)

                    val escapedText = text
                        .replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
                        .replace("\"", "&quot;")
                        .replace("'", "&apos;")

                    val lang = if (voice.startsWith("en-GB") || voice.startsWith("en-AU")) {
                        voice.substring(0, 5)
                    } else "en-US"

                    val ssmlMsg = "X-RequestId:$requestId\r\n" +
                            "Content-Type:application/ssml+xml\r\n" +
                            "Path:ssml\r\n\r\n" +
                            "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' " +
                            "xml:lang='$lang'>" +
                            "<voice name='$voice'>" +
                            "<prosody pitch='-5%'>$escapedText</prosody>" +
                            "</voice></speak>"
                    webSocket.send(ssmlMsg)
                    Log.d("EdgeTTS", "SSML sent")
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    try {
                        val data = bytes.toByteArray()
                        if (data.size > 2) {
                            val headerLen = ((data[0].toInt() and 0xFF) shl 8) or
                                    (data[1].toInt() and 0xFF)
                            if (headerLen < data.size) {
                                val headerStr = String(data, 2, headerLen)
                                if (headerStr.contains("Path:audio")) {
                                    val audioStart = 2 + headerLen
                                    if (audioStart < data.size) {
                                        outputStream.write(data, audioStart, data.size - audioStart)
                                        audioStarted = true
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("EdgeTTS", "Error processing audio chunk", e)
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    if (text.contains("turn.end")) {
                        Log.d("EdgeTTS", "Synthesis turn.end received, audioStarted=$audioStarted")
                        try { outputStream.close() } catch (_: Exception) {}
                        webSocket.close(1000, null)
                        if (!resumed) {
                            resumed = true
                            cont.resume(if (audioStarted) audioFile else null)
                        }
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e("EdgeTTS", "WebSocket failure: ${t.message}", t)
                    try { outputStream.close() } catch (_: Exception) {}
                    if (!resumed) {
                        resumed = true
                        cont.resume(null)
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d("EdgeTTS", "WebSocket closed: code=$code")
                    try { outputStream.close() } catch (_: Exception) {}
                    if (!resumed) {
                        resumed = true
                        cont.resume(if (audioStarted) audioFile else null)
                    }
                }
            })

            cont.invokeOnCancellation {
                ws.cancel()
                try { outputStream.close() } catch (_: Exception) {}
                audioFile.delete()
            }
        }

    private suspend fun playAudio(file: File) = suspendCancellableCoroutine { cont ->
        var resumed = false
        try {
            Log.d("EdgeTTS", "Playing audio: ${file.absolutePath} (${file.length()} bytes)")
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnCompletionListener {
                    Log.d("EdgeTTS", "Playback complete")
                    if (!resumed) {
                        resumed = true
                        cont.resume(Unit)
                    }
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("EdgeTTS", "Playback error: what=$what extra=$extra")
                    if (!resumed) {
                        resumed = true
                        cont.resume(Unit)
                    }
                    true
                }
                prepare()
                start()
                Log.d("EdgeTTS", "Playback started")
            }

            cont.invokeOnCancellation {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            }
        } catch (e: Exception) {
            Log.e("EdgeTTS", "Playback setup error", e)
            if (!resumed) {
                resumed = true
                cont.resume(Unit)
            }
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {}
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    companion object {
        val VOICES = listOf(
            "en-US-AndrewMultilingualNeural" to "Andrew (warm, natural)",
            "en-US-DavisNeural" to "Davis (calm, smooth)",
            "en-US-BrandonNeural" to "Brandon (clear, confident)",
            "en-GB-RyanNeural" to "Ryan (British, deep)",
            "en-US-JasonNeural" to "Jason (casual, friendly)",
            "en-AU-WilliamNeural" to "William (Australian)",
            "en-US-TonyNeural" to "Tony (conversational)",
            "en-GB-ThomasNeural" to "Thomas (British, mature)",
        )

        private const val WSS_URL = "wss://speech.platform.bing.com/consumer/speech/synthesize/readaloud/edge/v1"
        private const val TOKEN = "6A5AA1D4EAFF4E9FB37E23D68491D6F4"
    }
}
