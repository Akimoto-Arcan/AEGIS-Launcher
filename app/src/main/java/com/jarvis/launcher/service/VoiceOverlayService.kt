package com.jarvis.launcher.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.jarvis.launcher.domain.voice.VoicePipeline
import com.jarvis.launcher.domain.voice.VoiceState
import com.jarvis.launcher.ui.assistant.AssistantOverlay
import com.jarvis.launcher.ui.assistant.AssistantViewModel
import com.jarvis.launcher.ui.theme.JarvisTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class VoiceOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner,
    SavedStateRegistryOwner {

    @Inject lateinit var voicePipeline: VoicePipeline
    @Inject lateinit var ttsManager: com.jarvis.launcher.domain.voice.TextToSpeechManager

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = store

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showOverlay()

        scope.launch {
            voicePipeline.state.collect { state ->
                if (state is VoiceState.Idle) {
                    // Auto-hide handled by compose animation
                }
            }
        }
    }

    private fun showOverlay() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@VoiceOverlayService)
            setViewTreeSavedStateRegistryOwner(this@VoiceOverlayService)
            setContent {
                JarvisTheme {
                    OverlayContent()
                }
            }
        }

        windowManager?.addView(overlayView, params)
    }

    @Composable
    private fun OverlayContent() {
        val viewModel = AssistantViewModel(voicePipeline, ttsManager)
        AssistantOverlay(viewModel = viewModel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_ACTIVATE -> {
                scope.launch {
                    voicePipeline.onWakeWordDetected()
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        overlayView?.let { windowManager?.removeView(it) }
        overlayView = null
        scope.cancel()
        store.clear()
        super.onDestroy()
    }

    companion object {
        const val ACTION_ACTIVATE = "com.jarvis.launcher.ACTION_ACTIVATE"
    }
}
