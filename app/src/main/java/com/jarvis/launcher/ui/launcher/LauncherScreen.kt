package com.jarvis.launcher.ui.launcher

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.launcher.service.WakeWordService
import com.jarvis.launcher.ui.assistant.AssistantOverlay
import com.jarvis.launcher.ui.assistant.AssistantViewModel
import com.jarvis.launcher.ui.launcher.drawer.AppDrawerScreen
import com.jarvis.launcher.ui.launcher.home.HomeScreen
import com.jarvis.launcher.util.PermissionUtil

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherScreen(
    launcherViewModel: LauncherViewModel = hiltViewModel(),
    assistantViewModel: AssistantViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val apps by launcherViewModel.filteredApps.collectAsState()
    val searchQuery by launcherViewModel.searchQuery.collectAsState()
    val pagerState = rememberPagerState(initialPage = 0) { 2 }

    var permissionsGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionsGranted = results.values.all { it }
        if (permissionsGranted) {
            val serviceIntent = Intent(context, WakeWordService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }

    LaunchedEffect(Unit) {
        if (PermissionUtil.hasRecordAudioPermission(context) &&
            PermissionUtil.hasNotificationPermission(context)
        ) {
            permissionsGranted = true
            val serviceIntent = Intent(context, WakeWordService::class.java)
            context.startForegroundService(serviceIntent)
        } else {
            permissionLauncher.launch(PermissionUtil.requiredPermissions)
        }

        if (!PermissionUtil.hasOverlayPermission(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            when (page) {
                0 -> HomeScreen(
                    onJarvisActivate = { assistantViewModel.onManualActivate() }
                )
                1 -> AppDrawerScreen(
                    apps = apps,
                    searchQuery = searchQuery,
                    onSearchQueryChanged = launcherViewModel::onSearchQueryChanged,
                    onAppClick = launcherViewModel::launchApp
                )
            }
        }

        AssistantOverlay(viewModel = assistantViewModel)
    }
}
