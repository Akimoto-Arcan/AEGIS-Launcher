package com.jarvis.launcher.ui.launcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@AndroidEntryPoint
class LauncherActivity : ComponentActivity() {

    private val _homePressed = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val homePressed = _homePressed.asSharedFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LauncherScreen(homePressed = homePressed)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        _homePressed.tryEmit(Unit)
    }

    @Deprecated("Launcher consumes back press")
    override fun onBackPressed() {
        // Handled by BackHandler in Compose
        super.onBackPressed()
    }
}
