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

    suspend fun speak(text: String, voice: String = VOICE_JARVIS): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val audioFile = synthesize(text, voice) ?: return@withContext false
                playAudio(audioFile)
                audioFile.delete()
                true
            } catch (e: Exception) {
                Log.e("EdgeTTS", "Failed to speak", e)
                false
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

            val ws = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    val configMsg = "Content-Type:application/json; charset=utf-8\r\n" +
                            "Path:speech.config\r\n\r\n" +
                            """{"context":{"synthesis":{"audio":{"metadataoptions":{""" +
                            """"sentenceBoundaryEnabled":"false","wordBoundaryEnabled":"false"},""" +
                            """"outputFormat":"audio-24khz-48kbitrate-mono-mp3"}}}}"""
                    webSocket.send(configMsg)

                    val escapedText = text
                        .replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
                        .replace("\"", "&quot;")
                        .replace("'", "&apos;")

                    val ssmlMsg = "X-RequestId:$requestId\r\n" +
                            "Content-Type:application/ssml+xml\r\n" +
                            "Path:ssml\r\n\r\n" +
                            "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' " +
                            "xml:lang='en-GB'>" +
                            "<voice name='$voice'>" +
                            "<prosody rate='+5%' pitch='-5%'>$escapedText</prosody>" +
                            "</voice></speak>"
                    webSocket.send(ssmlMsg)
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    try {
                        val data = bytes.toByteArray()
                        // Audio data has a header: first 2 bytes = header length (big-endian)
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
                        try { outputStream.close() } catch (_: Exception) {}
                        webSocket.close(1000, null)
                        if (!resumed) {
                            resumed = true
                            cont.resume(if (audioStarted) audioFile else null)
                        }
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e("EdgeTTS", "WebSocket failure", t)
                    try { outputStream.close() } catch (_: Exception) {}
                    if (!resumed) {
                        resumed = true
                        cont.resume(null)
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
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
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnCompletionListener {
                    if (!resumed) {
                        resumed = true
                        cont.resume(Unit)
                    }
                }
                setOnErrorListener { _, _, _ ->
                    if (!resumed) {
                        resumed = true
                        cont.resume(Unit)
                    }
                    true
                }
                prepare()
                start()
            }

            cont.invokeOnCancellation {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            }
        } catch (e: Exception) {
            Log.e("EdgeTTS", "Playback error", e)
            if (!resumed) {
                resumed = true
                cont.resume(Unit)
            }
        }
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        const val VOICE_JARVIS = "en-GB-RyanNeural"
        private const val WSS_URL = "wss://speech.platform.bing.com/consumer/speech/synthesize/readaloud/edge/v1"
        private const val TOKEN = "6A5AA1D4EAFF4E9FB37E23D68491D6F4"
    }
}
