package com.example.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UnderstoodItem
import com.example.language.NttLanguage
import com.example.language.NumberTranslator
import com.example.overlay.NttOverlayService
import com.example.overlay.OverlayStateHolder
import com.example.ui.components.GlassSurface
import com.example.ui.components.NttLogoSymbol
import com.example.ui.components.ResultCardView
import com.example.ui.components.VoiceRegionalExplainerSheet
import androidx.compose.material.icons.filled.Apps
import com.example.overlay.SelectedAppsManager
import com.example.speech.NttTtsManager
import com.example.ui.theme.CharcoalMedium
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryContainer
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberContainer
import com.example.ui.util.NttHaptics
import com.example.ui.util.tactileClickable

@Composable
fun HomeScreen(
    selectedLanguage: NttLanguage,
    onNavigateToSearch: () -> Unit,
    onNavigateToScenarios: () -> Unit,
    onNavigateToLanguages: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onSpeak: (String) -> Unit,
    onSaveToHistory: ((UnderstoodItem) -> Unit)? = null,
    onNavigateToCamera: (() -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null,
    onNavigateToApps: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val overlayState by OverlayStateHolder.state.collectAsState()

    var showVoiceExplainer by remember { mutableStateOf(false) }
    var voiceExplainerInitialQuery by remember { mutableStateOf<String?>(null) }

    var quickInputNumber by remember { mutableStateOf("55") }
    var quickResult by remember(quickInputNumber, selectedLanguage) {
        val num = quickInputNumber.toLongOrNull() ?: 55L
        mutableStateOf(NumberTranslator.translateNumber(num, selectedLanguage.code, "₹"))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IvoryBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Top Header Row: NTT Logo + Title + Language Chip + Settings Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NttLogoSymbol(size = 38.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "NTT",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                        Text(
                            text = "Not That Type",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedText
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Clickable Language Pill (Mobile friendly)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SagePrimaryContainer.copy(alpha = 0.85f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
                        modifier = Modifier
                            .clickable {
                                NttHaptics.performTick(context, haptics)
                                onNavigateToLanguages()
                            }
                            .testTag("home_language_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedLanguage.nativeName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SagePrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "▾",
                                style = MaterialTheme.typography.labelSmall,
                                color = SagePrimary
                            )
                        }
                    }

                    // Settings Button
                    if (onNavigateToSettings != null) {
                        Surface(
                            shape = CircleShape,
                            color = IvorySurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable {
                                    NttHaptics.performClick(context, haptics)
                                    onNavigateToSettings()
                                }
                                .testTag("btn_home_settings")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = CharcoalMedium,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Mobile-Friendly Hero Card with Prominent Voice Mic & Clean Buttons
        item {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_activation_card"),
                shape = RoundedCornerShape(22.dp),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NttLogoSymbol(size = 56.dp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "NTT is ready",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Instant clarity for numbers, confusing regional accents, price tags & bills.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedText,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // PRIMARY FULL-WIDTH BUTTON: Live Voice & Mic Section Trigger
                    Button(
                        onClick = {
                            NttHaptics.performClick(context, haptics)
                            onNavigateToSearch()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_home_voice_mic"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Voice & Mic Assistant",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Speak numbers or slang (pachpan, dedh sau)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // SECONDARY 2-COLUMN ROW (50/50 split, perfectly fitted on any mobile width)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                NttHaptics.performClick(context, haptics)
                                onNavigateToCamera?.invoke()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_home_camera"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SagePrimaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = SagePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Camera",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = SagePrimary
                            )
                        }

                        Button(
                            onClick = {
                                NttHaptics.performClick(context, haptics)
                                onNavigateToScenarios()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_try_ntt"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SagePrimaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = SagePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Practice",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = SagePrimary
                            )
                        }
                    }
                }
            }
        }

        // 3. Floating Bar • Apps Option (App Allowlist Trigger)
        item {
            val triggerApps by SelectedAppsManager.triggerApps.collectAsState()
            val activeAppsCount = triggerApps.count { it.isEnabled }

            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("floating_bar_apps_option_card"),
                shape = RoundedCornerShape(20.dp),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = SagePrimaryContainer,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Apps,
                                        contentDescription = null,
                                        tint = SagePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Floating Bar • Apps Option",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalText
                                )
                                Text(
                                    text = if (overlayState.isForegroundAppSelected)
                                        "Active in ${overlayState.currentForegroundAppName ?: "Selected App"}"
                                    else
                                        "$activeAppsCount Trigger Apps Configured",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (overlayState.isForegroundAppSelected) SagePrimary else WarmAmber,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Toggle overlay service on/off
                        Switch(
                            checked = overlayState.isServiceRunning,
                            onCheckedChange = { enable ->
                                NttHaptics.performClick(context, haptics)
                                if (enable) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                    } else {
                                        val serviceIntent = Intent(context, NttOverlayService::class.java)
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            context.startForegroundService(serviceIntent)
                                        } else {
                                            context.startService(serviceIntent)
                                        }
                                    }
                                } else {
                                    val serviceIntent = Intent(context, NttOverlayService::class.java)
                                    context.stopService(serviceIntent)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SagePrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "The floating bar appears instantly only inside selected apps on your device. It never appears on the Home Screen or unselected apps, and hides completely the moment you leave the app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val firstActiveApp = triggerApps.firstOrNull { it.isEnabled } ?: triggerApps.firstOrNull()

                    // Buttons: Open Apps Manager & Quick Test
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                NttHaptics.performClick(context, haptics)
                                onNavigateToApps?.invoke()
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .testTag("btn_manage_trigger_apps"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Select Apps",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                NttHaptics.performClick(context, haptics)
                                if (overlayState.isForegroundAppSelected) {
                                    SelectedAppsManager.handleForegroundPackage("com.android.launcher3")
                                } else if (firstActiveApp != null) {
                                    SelectedAppsManager.handleForegroundPackage(firstActiveApp.packageName)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_test_phonepe_toggle"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (overlayState.isForegroundAppSelected) "Test Leave" else if (firstActiveApp != null) "Test ${firstActiveApp.appName.take(8)}" else "Test App",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = SagePrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Control Center / Quick Settings Tile note
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SagePrimaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = SagePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Control Center Tile: Swipe down from top of Android screen to add and tap the NTT Mic tile anytime.",
                                style = MaterialTheme.typography.labelSmall,
                                color = CharcoalText,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. Quick Number Converter & Script Breakdown
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Instant Converter (${selectedLanguage.englishName})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                    Text(
                        text = "Change",
                        style = MaterialTheme.typography.labelMedium,
                        color = SagePrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable {
                                NttHaptics.performTick(context, haptics)
                                onNavigateToLanguages()
                            }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = quickInputNumber,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }
                        if (filtered.length <= 12) {
                            NttHaptics.performTick(context, haptics)
                            quickInputNumber = filtered
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_quick_number"),
                    placeholder = { Text("Enter number (e.g. 55, 78, 103)") },
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    leadingIcon = {
                        Text(
                            text = "₹",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SagePrimary,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                NttHaptics.performClick(context, haptics)
                                onNavigateToSearch()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice input",
                                tint = SagePrimary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SagePrimary,
                        unfocusedBorderColor = SubtleBorder,
                        focusedContainerColor = IvorySurface,
                        unfocusedContainerColor = IvorySurface
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                ResultCardView(
                    result = quickResult,
                    onSpeak = {
                        NttHaptics.performClick(context, haptics)
                        val speechText = NttTtsManager.buildIdentifiedNumberSpeech(quickResult)
                        onSpeak(speechText)
                    },
                    onExplainWithGemini = { text ->
                        voiceExplainerInitialQuery = text
                        showVoiceExplainer = true
                    }
                )
            }
        }

        // 5. Explore Features Grid
        item {
            Text(
                text = "Explore Features",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CharcoalText
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureTile(
                    title = "Test Scenarios & Practice",
                    subtitle = "Kirana store, Auto meter, Restaurant bill & Market prices",
                    icon = Icons.Default.PlayArrow,
                    onClick = onNavigateToScenarios,
                    testTag = "tile_scenarios"
                )

                FeatureTile(
                    title = "Phonetic Search & Voice",
                    subtitle = "Search 'pachpan', 'sattavan', 'dedh sau' or speak aloud",
                    icon = Icons.Default.Search,
                    onClick = onNavigateToSearch,
                    testTag = "tile_search"
                )

                FeatureTile(
                    title = "11 Indian Regional Languages",
                    subtitle = "Hindi, Marathi, Gujarati, Bengali, Tamil, Telugu & more",
                    icon = Icons.Default.Translate,
                    onClick = onNavigateToLanguages,
                    testTag = "tile_languages"
                )

                FeatureTile(
                    title = "History & Saved Words",
                    subtitle = "Review previously understood numbers and favorites",
                    icon = Icons.Default.History,
                    onClick = onNavigateToHistory,
                    testTag = "tile_history"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showVoiceExplainer) {
        VoiceRegionalExplainerSheet(
            onDismiss = { showVoiceExplainer = false },
            onSaveItem = onSaveToHistory,
            onSpeak = onSpeak,
            initialQuery = voiceExplainerInitialQuery,
            languageLocaleTag = selectedLanguage.ttsLocaleTag
        )
    }
}

@Composable
private fun FeatureTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .tactileClickable {
                NttHaptics.performClick(context, haptics)
                onClick()
            }
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = IvorySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SagePrimaryContainer,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SagePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MutedText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
