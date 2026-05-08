package com.jarvis.launcher.ui.launcher

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.launcher.data.local.PreferencesStore
import com.jarvis.launcher.data.repository.WeatherData
import com.jarvis.launcher.data.repository.WeatherRepository
import com.jarvis.launcher.service.WakeWordService
import com.jarvis.launcher.ui.assistant.AssistantOverlay
import com.jarvis.launcher.ui.assistant.AssistantViewModel
import com.jarvis.launcher.ui.launcher.drawer.AppDrawerScreen
import com.jarvis.launcher.ui.launcher.home.HomeScreen
import com.jarvis.launcher.ui.settings.SettingsScreen
import com.jarvis.launcher.ui.settings.SettingsViewModel
import com.jarvis.launcher.ui.theme.HudColors
import com.jarvis.launcher.ui.theme.JarvisTheme
import com.jarvis.launcher.util.PermissionUtil
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherScreen(
    launcherViewModel: LauncherViewModel = hiltViewModel(),
    assistantViewModel: AssistantViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    homePressed: SharedFlow<Unit> = remember { kotlinx.coroutines.flow.MutableSharedFlow() }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apps by launcherViewModel.filteredApps.collectAsState()
    val allApps by launcherViewModel.apps.collectAsState()
    val searchQuery by launcherViewModel.searchQuery.collectAsState()
    val favorites by settingsViewModel.favoriteApps.collectAsState()
    val colorThemeName by settingsViewModel.colorThemeName.collectAsState()
    val useFahrenheit by settingsViewModel.useFahrenheit.collectAsState()
    val pagerState = rememberPagerState(initialPage = 0) { 2 }

    var showSettings by remember { mutableStateOf(!settingsViewModel.hasApiKey()) }
    var permissionsGranted by remember { mutableStateOf(false) }
    var weatherData by remember { mutableStateOf<WeatherData?>(null) }
    val weatherRepository = remember { WeatherRepository() }

    val theme = PreferencesStore.getThemeByName(colorThemeName)
    val hudColors = HudColors(
        accent = Color(theme.primary),
        accentDark = Color(theme.primaryDark),
        accentGlow = Color(theme.glow)
    )

    // Fetch weather
    LaunchedEffect(Unit) {
        weatherData = weatherRepository.getWeather()
    }

    // Handle home button: return to home page, close settings
    LaunchedEffect(Unit) {
        homePressed.collect {
            showSettings = false
            if (pagerState.currentPage != 0) {
                pagerState.animateScrollToPage(0)
            }
        }
    }

    // Handle back button: go to home page or close settings
    BackHandler(enabled = showSettings || pagerState.currentPage != 0) {
        if (showSettings) {
            showSettings = false
        } else if (pagerState.currentPage != 0) {
            scope.launch { pagerState.animateScrollToPage(0) }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionsGranted = results.values.all { it }
    }

    LaunchedEffect(Unit) {
        if (PermissionUtil.hasRecordAudioPermission(context) &&
            PermissionUtil.hasNotificationPermission(context)
        ) {
            permissionsGranted = true
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

    JarvisTheme(hudColors = hudColors) {
        if (showSettings) {
            SettingsScreen(
                viewModel = settingsViewModel,
                installedApps = allApps,
                onDone = { showSettings = false }
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1
                ) { page ->
                    when (page) {
                        0 -> HomeScreen(
                            onJarvisActivate = { assistantViewModel.onManualActivate() },
                            onSettingsOpen = { showSettings = true },
                            favoritePackages = favorites,
                            onOrbitAppClick = { pkg ->
                                val app = allApps.find { it.packageName == pkg }
                                app?.let { launcherViewModel.launchApp(it) }
                            },
                            weatherData = weatherData,
                            useFahrenheit = useFahrenheit
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
    }
}
