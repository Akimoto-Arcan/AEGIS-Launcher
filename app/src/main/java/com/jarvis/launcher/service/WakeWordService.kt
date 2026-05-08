package com.jarvis.launcher.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.jarvis.launcher.domain.voice.VoicePipeline
import com.jarvis.launcher.domain.voice.WakeWordDetector
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WakeWordService : Service() {

    @Inject lateinit var wakeWordDetector: WakeWordDetector
    @Inject lateinit var voicePipeline: VoicePipeline

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        scope.launch(Dispatchers.IO) {
            wakeWordDetector.startListening()
        }

        scope.launch {
            wakeWordDetector.detections.collect {
                voicePipeline.onWakeWordDetected()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        wakeWordDetector.stopListening()
        scope.cancel()
        super.onDestroy()
    }
}
