package com.example.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSettings
import com.example.model.PreferencesManager
import com.example.overlay.OverlayStateHolder
import com.example.speech.LocalNttTtsManager
import com.example.speech.NttTtsManager
import com.example.ui.components.GlassSurface
import com.example.ui.components.ListenSpeakerButton
import com.example.ui.components.NttLogoSymbol
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

@Composable
fun SettingsScreen(
    prefsManager: PreferencesManager,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToApps: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val overlayState by OverlayStateHolder.state.collectAsState()
    var currentSettings by remember { mutableStateOf(prefsManager.getSettings()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IvoryBackground)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            if (onNavigateBack != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CharcoalText
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                }
            }

            Text(
                text = "Settings & Privacy Hub",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = CharcoalText
            )

            Text(
                text = "Control how NTT behaves and review privacy protections.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedText
            )
        }

        // Section 1: Privacy Hub Reassurance
        item {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("privacy_hub_card"),
                shape = RoundedCornerShape(22.dp),
                elevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SagePrimaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Privacy Shield",
                                    tint = SagePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Privacy & Safety Guarantee",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )
                            Text(
                                text = "100% On-Device Processing",
                                style = MaterialTheme.typography.labelSmall,
                                color = SagePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    PrivacyBullet(
                        icon = Icons.Default.Check,
                        text = "NTT does not need permission to control your phone."
                    )
                    PrivacyBullet(
                        icon = Icons.Default.Check,
                        text = "All number translation runs offline on your device."
                    )
                    PrivacyBullet(
                        icon = Icons.Default.Block,
                        text = "Passwords, PINs, OTPs, and card numbers are never read or stored."
                    )
                    PrivacyBullet(
                        icon = Icons.Default.Lock,
                        text = "No analytics, tracking, or cloud uploading."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                } catch (e: Exception) {
                                    // fallback
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Accessibility",
                                style = MaterialTheme.typography.labelMedium,
                                color = SagePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    try {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // fallback
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Overlay Perms",
                                style = MaterialTheme.typography.labelMedium,
                                color = SagePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Section: Floating Bar Trigger Apps
        item {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_apps_trigger_card"),
                shape = RoundedCornerShape(20.dp),
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Trigger Apps (Allowlist)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose which installed apps on your device trigger the floating bar. The bar appears instantly when an enabled app is opened, and hides completely on the Home Screen or other apps.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onNavigateToApps?.invoke() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_settings_select_apps"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                    ) {
                        Text(
                            text = "Manage Trigger Apps",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Section: Floating Bar Display Duration (User Selection: Always, 5m, 10m, etc.)
        item {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_duration_card"),
                shape = RoundedCornerShape(20.dp),
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Floating Bar Display Duration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose how long the floating button remains displayed inside selected apps without hiding or blinking.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val durations = listOf(
                        0 to "Always",
                        5 to "5m",
                        10 to "10m",
                        15 to "15m",
                        30 to "30m"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        durations.forEach { (minutes, label) ->
                            val isSelected = overlayState.durationMinutes == minutes
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    OverlayStateHolder.setDurationMinutes(minutes, context)
                                },
                                label = {
                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SagePrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (overlayState.durationMinutes == 0)
                            "✓ Always Display: The shortcut button never hides while inside selected apps, continuously detecting screen numbers without blinking."
                        else
                            "✓ Stays visible for ${overlayState.durationMinutes} minutes inside selected apps. User interaction or screen detection refreshes the timer.",
                        style = MaterialTheme.typography.labelSmall,
                        color = SagePrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Section: Real-Time Screen Detection & Automatic UI Pop-up
        item {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_auto_popup_card"),
                shape = RoundedCornerShape(20.dp),
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Real-Time Highlights & Auto Pop-up",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "NTT scans detected numbers and currency amounts in selected apps in real time, showing immediate visual highlights.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Automatic UI Pop-up",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = CharcoalText
                            )
                            Text(
                                text = "Automatically open explanation when a new amount appears (by user permission)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedText
                            )
                        }
                        Switch(
                            checked = overlayState.isAutoPopupEnabled,
                            onCheckedChange = { enabled ->
                                OverlayStateHolder.setAutoPopupEnabled(enabled, context)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SagePrimary
                            ),
                            modifier = Modifier.testTag("switch_auto_popup_permission")
                        )
                    }
                }
            }
        }

        // Section: TextToSpeech (TTS) Engine & Voice Readback
        item {
            val ttsManager = LocalNttTtsManager.current ?: remember { NttTtsManager.getInstance(context) }
            var selectedRate by remember { mutableStateOf(ttsManager.currentSpeechRate) }
            val samplePriceSpeech = "₹2,500. Two Thousand Five Hundred Rupees. Do Hazaar Paanch Sau Rupaye. Also known as Dhaai Hazaar."

            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_tts_engine_card"),
                shape = RoundedCornerShape(20.dp),
                elevation = 2.dp
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
                        Column {
                            Text(
                                text = "Text-to-Speech Engine",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )
                            Text(
                                text = "Reads back identified numbers, prices & words",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedText
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SagePrimaryContainer.copy(alpha = 0.8f)
                        ) {
                            Text(
                                text = "Ready",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SagePrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Speech Speed / Pacing:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalMedium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("Slow (0.80x)", 0.80f, "rate_slow"),
                            Triple("Normal (0.95x)", 0.95f, "rate_normal"),
                            Triple("Fast (1.20x)", 1.20f, "rate_fast")
                        ).forEach { (label, rate, tag) ->
                            val isSelected = Math.abs(selectedRate - rate) < 0.05f
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) SagePrimary else IvorySurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) SagePrimary else SubtleBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedRate = rate
                                        ttsManager.setSpeechRate(rate)
                                    }
                                    .testTag(tag)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else CharcoalText,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Test TTS Audio button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Test Voice Readback",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )
                            Text(
                                text = "Listen to sample identified price read aloud",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedText
                            )
                        }

                        ListenSpeakerButton(
                            text = "Sample",
                            spokenText = samplePriceSpeech,
                            languageTag = "hi-IN",
                            showLabel = true,
                            labelText = "🔊 Test Audio",
                            containerColor = SagePrimaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.testTag("btn_test_tts_audio")
                        )
                    }
                }
            }
        }

        // Section 2: Understanding Features
        item {
            Text(
                text = "Understanding Features",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CharcoalText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingToggleRow(
                    title = "Understand Numbers (0–100, Lakh, Crore)",
                    subtitle = "Translates numerals to native words and breakdowns",
                    checked = currentSettings.understandNumbers,
                    onCheckedChange = {
                        prefsManager.updateToggle("understand_numbers", it)
                        currentSettings = prefsManager.getSettings()
                    },
                    testTag = "toggle_understand_numbers"
                )

                SettingToggleRow(
                    title = "Understand Currency (₹, Rs, Rupaye)",
                    subtitle = "Identifies Indian rupees, paisa, and currency amounts",
                    checked = currentSettings.understandCurrency,
                    onCheckedChange = {
                        prefsManager.updateToggle("understand_currency", it)
                        currentSettings = prefsManager.getSettings()
                    },
                    testTag = "toggle_understand_currency"
                )

                SettingToggleRow(
                    title = "Voice & Spoken Word Recognition",
                    subtitle = "Recognizes phrases like 'pachpan rupaye' or 'dedh sau'",
                    checked = currentSettings.understandVoice,
                    onCheckedChange = {
                        prefsManager.updateToggle("understand_voice", it)
                        currentSettings = prefsManager.getSettings()
                    },
                    testTag = "toggle_understand_voice"
                )

                SettingToggleRow(
                    title = "Automatic Sensitive Content Protection",
                    subtitle = "Masks card numbers, PINs, OTPs, and password fields automatically",
                    checked = currentSettings.sensitiveProtectionEnabled,
                    onCheckedChange = {
                        prefsManager.updateToggle("sensitive_protection", it)
                        currentSettings = prefsManager.getSettings()
                    },
                    testTag = "toggle_sensitive_protection"
                )
            }
        }

        // Section 3: About NTT
        item {
            Text(
                text = "About NTT",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CharcoalText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = IvorySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NttLogoSymbol(size = 32.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "NTT (Not That Type)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )
                            Text(
                                text = "Version 2.1.3 • On-Device Accessibility Engine",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Designed to provide calm, instant clarity for numbers, prices, and spoken regional words across 11 Indian languages.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PrivacyBullet(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SagePrimary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = CharcoalMedium
        )
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = IvorySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = CharcoalText
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = SagePrimary
                ),
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}
