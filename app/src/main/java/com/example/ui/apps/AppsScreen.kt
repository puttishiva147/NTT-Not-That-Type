package com.example.ui.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.accessibility.NttAccessibilityService
import com.example.overlay.OverlayStateHolder
import com.example.overlay.SelectedAppsManager
import com.example.overlay.TriggerApp
import com.example.ui.components.GlassSurface
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    // Trigger immediate refresh of genuine device installed apps upon opening
    LaunchedEffect(Unit) {
        SelectedAppsManager.refreshInstalledApps(context)
    }

    val triggerApps by SelectedAppsManager.triggerApps.collectAsState()
    val overlayState by OverlayStateHolder.state.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val categories = remember(triggerApps) {
        listOf("All", "Selected Only") + triggerApps.map { it.category }.distinct()
    }

    val filteredApps = remember(triggerApps, searchQuery, selectedCategoryFilter) {
        triggerApps.filter { app ->
            val matchesCategory = when (selectedCategoryFilter) {
                "All" -> true
                "Selected Only" -> app.isEnabled
                else -> app.category == selectedCategoryFilter
            }
            val matchesSearch = searchQuery.isBlank() ||
                    app.appName.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    val enabledCount = triggerApps.count { it.isEnabled }
    val totalDeviceApps = triggerApps.size
    val isAccessibilityActive = overlayState.isAccessibilityConnected || NttAccessibilityService.isConnected()
    val canDrawOverlays = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IvoryBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        NttHaptics.performClick(context, haptics)
                        onNavigateBack()
                    },
                    modifier = Modifier.testTag("btn_apps_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CharcoalText
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Trigger Apps",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                    Text(
                        text = "$totalDeviceApps genuine apps detected on this device",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedText
                    )
                }

                // Rescan button
                IconButton(
                    onClick = {
                        NttHaptics.performClick(context, haptics)
                        SelectedAppsManager.refreshInstalledApps(context)
                        Toast.makeText(context, "Rescanned installed apps ($totalDeviceApps found)", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("btn_refresh_installed_apps")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Scan Device Apps",
                        tint = SagePrimary
                    )
                }
            }
        }

        // 2. Real Device Verification & Status Card
        item {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apps_overview_card"),
                shape = RoundedCornerShape(20.dp),
                elevation = 3.dp
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
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = SagePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "$enabledCount of $totalDeviceApps Apps Selected",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalText
                                )
                                Text(
                                    text = if (overlayState.isForegroundAppSelected)
                                        "Floating bar active in ${overlayState.currentForegroundAppName ?: "selected app"}"
                                    else
                                        "Hidden (Outside selected apps)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (overlayState.isForegroundAppSelected) SagePrimary else MutedText,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (overlayState.isForegroundAppSelected) SagePrimary.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (overlayState.isForegroundAppSelected) "Bar Visible" else "Bar Hidden",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (overlayState.isForegroundAppSelected) SagePrimary else CharcoalMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "100% Real Device Apps. All apps listed below are detected directly from your device. The floating bar appears instantly when you open any selected app, and hides completely when you return home or switch apps.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText,
                        lineHeight = 18.sp
                    )

                    // Permission warning if required
                    if (!isAccessibilityActive || !canDrawOverlays) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = WarmAmberContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = WarmAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Permissions Required for Automatic Detection",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalText
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Enable Accessibility to detect app launches and Display Over Other Apps so the bar can float over them.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedText,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (!isAccessibilityActive) {
                                        Button(
                                            onClick = {
                                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                                context.startActivity(intent)
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = WarmAmber),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Accessibility", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    if (!canDrawOverlays) {
                                        Button(
                                            onClick = {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    val intent = Intent(
                                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                        Uri.parse("package:${context.packageName}")
                                                    )
                                                    context.startActivity(intent)
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Overlay Permission", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Floating Bar Display Duration (User Selection: Always, 5m, 10m, etc.)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = IvorySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_floating_bar_duration")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Floating Bar Display Duration",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Choose how long the floating button remains displayed inside selected apps without hiding or blinking.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

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
                                    NttHaptics.performClick(context, haptics)
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

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (overlayState.durationMinutes == 0)
                            "✓ Always Display: The shortcut button never hides while inside selected apps, providing continuous screen number detection without blinking."
                        else
                            "✓ Stays visible for ${overlayState.durationMinutes} minutes inside selected apps. Touching or screen detection activity refreshes the timer.",
                        style = MaterialTheme.typography.labelSmall,
                        color = SagePrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 3. Quick Behavior Tester with Actual Device Apps
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = IvorySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Instant Visibility Test",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                    Text(
                        text = "Verify that the bar appears only inside selected apps and vanishes on Home",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val firstEnabledApp = triggerApps.firstOrNull { it.isEnabled }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                NttHaptics.performClick(context, haptics)
                                if (firstEnabledApp != null) {
                                    SelectedAppsManager.handleForegroundPackage(firstEnabledApp.packageName)
                                } else {
                                    Toast.makeText(context, "Select at least one app below first", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp), tint = SagePrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (firstEnabledApp != null) "In ${firstEnabledApp.appName.take(10)}" else "Test Selected",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SagePrimary
                            )
                        }

                        Button(
                            onClick = {
                                NttHaptics.performClick(context, haptics)
                                // Simulates going back to Android home screen
                                SelectedAppsManager.handleForegroundPackage("com.android.launcher3")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalText)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Go Home", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // 4. Search and Add Real Installed Package
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search installed apps...", fontSize = 13.sp, color = MutedText) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = SagePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_search_apps"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SagePrimary,
                        unfocusedBorderColor = SubtleBorder,
                        focusedContainerColor = IvorySurface,
                        unfocusedContainerColor = IvorySurface
                    ),
                    singleLine = true
                )

                OutlinedButton(
                    onClick = {
                        NttHaptics.performClick(context, haptics)
                        showAddDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("btn_add_app_package")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Package",
                        tint = SagePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add App", fontSize = 12.sp, color = SagePrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // 5. Category Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.take(4).forEach { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            NttHaptics.performClick(context, haptics)
                            selectedCategoryFilter = cat
                        },
                        label = {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SagePrimaryContainer,
                            selectedLabelColor = SagePrimary,
                            containerColor = IvorySurface,
                            labelColor = CharcoalText
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) SagePrimary else SubtleBorder
                        )
                    )
                }
            }
        }

        // 6. Installed Apps List
        if (filteredApps.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = IvorySurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = MutedText,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No apps match '$searchQuery'" else "No apps found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the refresh button in the top right to re-scan this device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedText
                        )
                    }
                }
            }
        } else {
            items(filteredApps, key = { it.packageName }) { app ->
                RealAppRow(
                    app = app,
                    onToggle = { enabled ->
                        NttHaptics.performClick(context, haptics)
                        SelectedAppsManager.toggleApp(app.packageName, enabled)
                    },
                    onLaunch = {
                        try {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                            if (launchIntent != null) {
                                context.startActivity(launchIntent)
                            } else {
                                Toast.makeText(context, "Cannot launch ${app.appName} directly", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open app: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialog for adding an app by package name with real-device validation
    if (showAddDialog) {
        var inputPackage by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Add Device App",
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter the package name of an app installed on your device (e.g. com.google.android.apps.messaging):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = inputPackage,
                        onValueChange = {
                            inputPackage = it
                            errorMessage = null
                        },
                        placeholder = { Text("e.g. com.android.chrome", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_custom_package_name"),
                        singleLine = true,
                        isError = errorMessage != null
                    )
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val result = SelectedAppsManager.addInstalledAppByPackage(inputPackage, context)
                        if (result.isSuccess) {
                            val added = result.getOrNull()
                            Toast.makeText(context, "Added ${added?.appName ?: inputPackage}", Toast.LENGTH_SHORT).show()
                            showAddDialog = false
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "App is not installed on this device"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                    modifier = Modifier.testTag("btn_confirm_add_package")
                ) {
                    Text("Add & Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = MutedText)
                }
            }
        )
    }
}

@Composable
private fun RealAppRow(
    app: TriggerApp,
    onToggle: (Boolean) -> Unit,
    onLaunch: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = IvorySurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (app.isEnabled) SagePrimary.copy(alpha = 0.4f) else SubtleBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_row_${app.packageName}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Real App Icon from PackageManager
            RealAppIcon(
                packageName = app.packageName,
                contentDescription = app.appName,
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText,
                        maxLines = 1
                    )
                    if (app.isEnabled) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Enabled",
                            tint = SagePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SagePrimaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = app.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = SagePrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (app.isSystemApp) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.LightGray.copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = "System",
                                style = MaterialTheme.typography.labelSmall,
                                color = CharcoalMedium,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Quick launch button
            IconButton(
                onClick = onLaunch,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("btn_launch_${app.packageName}")
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "Open ${app.appName}",
                    tint = SagePrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Trigger switch
            Switch(
                checked = app.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = SagePrimary
                ),
                modifier = Modifier.testTag("switch_${app.packageName}")
            )
        }
    }
}

/**
 * Loads and renders the authentic application icon directly from Android's PackageManager.
 */
@Composable
private fun RealAppIcon(
    packageName: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val iconBitmap = remember(packageName) {
        try {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            convertDrawableToBitmap(drawable)
        } catch (e: Exception) {
            null
        }
    }

    if (iconBitmap != null) {
        Image(
            bitmap = iconBitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier.clip(RoundedCornerShape(10.dp))
        )
    } else {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = SagePrimaryContainer,
            modifier = modifier
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = null,
                    tint = SagePrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

private fun convertDrawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
