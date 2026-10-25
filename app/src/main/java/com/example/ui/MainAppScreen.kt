package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UnderstoodItem
import com.example.data.UnderstoodRepository
import com.example.language.NttLanguage
import com.example.language.SupportedLanguages
import com.example.model.PreferencesManager
import com.example.speech.LocalNttTtsManager
import com.example.speech.NttTtsManager
import com.example.speech.VoiceRecognizerHelper
import com.example.ui.apps.AppsScreen
import com.example.ui.camera.CameraCaptureScreen
import com.example.ui.history.HistoryScreen
import com.example.ui.home.HomeScreen
import com.example.ui.languages.LanguagesScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.search.SearchScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.test.TestScenariosScreen
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryContainer
import kotlinx.coroutines.launch

@Composable
fun MainAppScreen(
    repository: UnderstoodRepository,
    prefsManager: PreferencesManager,
    ttsManager: NttTtsManager,
    voiceRecognizer: VoiceRecognizerHelper?,
    openMicOnStart: Boolean = false,
    onOpenMicHandled: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var appSettings by remember { mutableStateOf(prefsManager.getSettings()) }
    var isOnboardingDone by remember { mutableStateOf(appSettings.isOnboardingCompleted) }
    var selectedLanguage by remember {
        mutableStateOf(SupportedLanguages.getByCode(appSettings.defaultLanguageCode))
    }

    var selectedNavTab by remember { mutableIntStateOf(0) } // 0: Home, 1: Voice, 2: Camera, 3: Practice, 4: History
    var isCameraScreenOpen by remember { mutableStateOf(false) }
    var isLanguagesScreenOpen by remember { mutableStateOf(false) }
    var isSettingsScreenOpen by remember { mutableStateOf(false) }
    var isAppsScreenOpen by remember { mutableStateOf(false) }

    LaunchedEffect(openMicOnStart) {
        if (openMicOnStart) {
            selectedNavTab = 1
            isCameraScreenOpen = false
            isLanguagesScreenOpen = false
            isSettingsScreenOpen = false
            onOpenMicHandled()
        }
    }

    CompositionLocalProvider(LocalNttTtsManager provides ttsManager) {
        if (!isOnboardingDone) {
            OnboardingScreen(
                onComplete = { chosenLangCode ->
                    prefsManager.setDefaultLanguage(chosenLangCode)
                    prefsManager.setOnboardingCompleted(true)
                    appSettings = prefsManager.getSettings()
                    selectedLanguage = SupportedLanguages.getByCode(chosenLangCode)
                    isOnboardingDone = true
                }
            )
        } else if (isLanguagesScreenOpen) {
            LanguagesScreen(
                currentLanguage = selectedLanguage,
                onSelectLanguage = { lang ->
                    selectedLanguage = lang
                    prefsManager.setDefaultLanguage(lang.code)
                },
                onSpeak = { text -> ttsManager.speak(text, selectedLanguage.ttsLocaleTag) },
                onNavigateBack = { isLanguagesScreenOpen = false }
            )
        } else if (isAppsScreenOpen) {
            AppsScreen(
                onNavigateBack = { isAppsScreenOpen = false }
            )
        } else if (isSettingsScreenOpen) {
            SettingsScreen(
                prefsManager = prefsManager,
                onNavigateBack = { isSettingsScreenOpen = false },
                onNavigateToApps = { isAppsScreenOpen = true }
            )
        } else if (isCameraScreenOpen) {
            CameraCaptureScreen(
                selectedLanguage = selectedLanguage,
                onNavigateBack = { isCameraScreenOpen = false },
                onSpeak = { text: String -> ttsManager.speak(text, selectedLanguage.ttsLocaleTag) },
                onSaveToHistory = { item: com.example.data.UnderstoodItem ->
                    coroutineScope.launch { repository.insertItem(item) }
                }
            )
        } else {
            Scaffold(
                containerColor = IvoryBackground,
                bottomBar = {
                    NavigationBar(
                        containerColor = IvorySurface,
                        contentColor = SagePrimary,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("main_bottom_nav")
                    ) {
                        // 1. Home Tab
                        NavigationBarItem(
                            selected = selectedNavTab == 0,
                            onClick = { selectedNavTab = 0 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Home",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "Home",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selectedNavTab == 0) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SagePrimary,
                                selectedTextColor = SagePrimary,
                                indicatorColor = SagePrimaryContainer,
                                unselectedIconColor = MutedText,
                                unselectedTextColor = MutedText
                            ),
                            modifier = Modifier.testTag("nav_item_home")
                        )

                        // 2. Voice & Mic Tab
                        NavigationBarItem(
                            selected = selectedNavTab == 1,
                            onClick = { selectedNavTab = 1 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Assistant",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "Voice",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selectedNavTab == 1) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SagePrimary,
                                selectedTextColor = SagePrimary,
                                indicatorColor = SagePrimaryContainer,
                                unselectedIconColor = MutedText,
                                unselectedTextColor = MutedText
                            ),
                            modifier = Modifier.testTag("nav_item_voice")
                        )

                        // 3. Camera Scanner Tab
                        NavigationBarItem(
                            selected = selectedNavTab == 2,
                            onClick = {
                                isCameraScreenOpen = true
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Camera Scanner",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "Camera",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selectedNavTab == 2) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SagePrimary,
                                selectedTextColor = SagePrimary,
                                indicatorColor = SagePrimaryContainer,
                                unselectedIconColor = MutedText,
                                unselectedTextColor = MutedText
                            ),
                            modifier = Modifier.testTag("nav_item_camera")
                        )

                        // 4. Practice Tab
                        NavigationBarItem(
                            selected = selectedNavTab == 3,
                            onClick = { selectedNavTab = 3 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Practice",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "Practice",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selectedNavTab == 3) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SagePrimary,
                                selectedTextColor = SagePrimary,
                                indicatorColor = SagePrimaryContainer,
                                unselectedIconColor = MutedText,
                                unselectedTextColor = MutedText
                            ),
                            modifier = Modifier.testTag("nav_item_practice")
                        )

                        // 5. History Tab
                        NavigationBarItem(
                            selected = selectedNavTab == 4,
                            onClick = { selectedNavTab = 4 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "History",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "History",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selectedNavTab == 4) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SagePrimary,
                                selectedTextColor = SagePrimary,
                                indicatorColor = SagePrimaryContainer,
                                unselectedIconColor = MutedText,
                                unselectedTextColor = MutedText
                            ),
                            modifier = Modifier.testTag("nav_item_history")
                        )
                    }
                },
                modifier = modifier.fillMaxSize()
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    AnimatedContent(
                        targetState = selectedNavTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tab_content"
                    ) { tab ->
                        when (tab) {
                            0 -> HomeScreen(
                                selectedLanguage = selectedLanguage,
                                onNavigateToSearch = { selectedNavTab = 1 },
                                onNavigateToScenarios = { selectedNavTab = 3 },
                                onNavigateToLanguages = { isLanguagesScreenOpen = true },
                                onNavigateToHistory = { selectedNavTab = 4 },
                                onNavigateToSettings = { isSettingsScreenOpen = true },
                                onNavigateToApps = { isAppsScreenOpen = true },
                                onSpeak = { text -> ttsManager.speak(text, selectedLanguage.ttsLocaleTag) },
                                onSaveToHistory = { item ->
                                    coroutineScope.launch { repository.insertItem(item) }
                                },
                                onNavigateToCamera = { isCameraScreenOpen = true }
                            )
                            1 -> SearchScreen(
                                selectedLanguage = selectedLanguage,
                                voiceRecognizer = voiceRecognizer,
                                onSaveToHistory = { item ->
                                    coroutineScope.launch { repository.insertItem(item) }
                                },
                                onSpeak = { text -> ttsManager.speak(text, selectedLanguage.ttsLocaleTag) }
                            )
                            2 -> {
                                // Fallback if navigated to tab 2
                                LaunchedEffect(Unit) { isCameraScreenOpen = true }
                            }
                            3 -> TestScenariosScreen(
                                selectedLanguage = selectedLanguage,
                                onSpeak = { text -> ttsManager.speak(text, selectedLanguage.ttsLocaleTag) }
                            )
                            4 -> HistoryScreen(
                                repository = repository,
                                onSpeak = { text -> ttsManager.speak(text, selectedLanguage.ttsLocaleTag) },
                                onDeleteItem = { id ->
                                    coroutineScope.launch { repository.deleteItem(id) }
                                },
                                onToggleFavorite = { id, current ->
                                    coroutineScope.launch { repository.toggleFavorite(id, current) }
                                },
                                onClearAll = {
                                    coroutineScope.launch { repository.clearHistory() }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
