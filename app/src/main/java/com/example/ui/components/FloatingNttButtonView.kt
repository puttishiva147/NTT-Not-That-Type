package com.example.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.accessibility.NttAccessibilityService
import com.example.overlay.OverlayStateHolder
import com.example.speech.NttTtsManager
import com.example.speech.VoiceRecognizerHelper
import com.example.speech.VoiceState
import com.example.ui.theme.CharcoalMedium
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.MutedText
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryContainer
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberContainer
import com.example.ui.util.NttHaptics
import com.example.ui.util.tactileClickable

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FloatingNttOverlayComposeContent(
    modifier: Modifier = Modifier,
    languageCode: String = "hi",
    onDrag: ((Float, Float) -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    onOpenSearch: (() -> Unit)? = null,
    onSpeak: ((String) -> Unit)? = null
) {
    val overlayState by OverlayStateHolder.state.collectAsState()
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    // Voice recognizer helper for the live mic section
    val voiceRecognizer = remember { VoiceRecognizerHelper(context) }
    val voiceState by voiceRecognizer.voiceState.collectAsState()

    // 0: Live Mic Section, 1: Type / Screen Scan
    var selectedOverlayMode by remember { mutableIntStateOf(0) }

    // Pulse animation for listening state
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Listen to voice recognition results
    LaunchedEffect(voiceState) {
        if (voiceState is VoiceState.Success) {
            val recognizedText = (voiceState as VoiceState.Success).recognizedText
            OverlayStateHolder.showVoiceResult(recognizedText, languageCode)
        }
    }

    AnimatedVisibility(
        visible = overlayState.isButtonVisible && overlayState.isForegroundAppSelected,
        enter = fadeIn(
            animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
        ) + scaleIn(
            initialScale = 0.75f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ),
        exit = fadeOut(
            animationSpec = tween(durationMillis = 250, easing = FastOutLinearInEasing)
        ) + scaleOut(
            targetScale = 0.75f,
            animationSpec = tween(durationMillis = 250)
        )
    ) {
        Column(
            modifier = modifier
                .widthIn(max = 380.dp)
                .padding(6.dp),
            horizontalAlignment = Alignment.End
        ) {
            // 1. Sleek, Compact Floating Shortcut Bar (Never duplicates or covers screen content)
            val liveHighlights = overlayState.liveHighlights
            AnimatedVisibility(
                visible = !overlayState.isPanelExpanded,
                enter = fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.85f),
                exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.85f)
            ) {
                var isBarPressed by remember { mutableStateOf(false) }
                val barScale by animateFloatAsState(
                    targetValue = if (isBarPressed) 0.94f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "bar_scale"
                )

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF1B2C23),
                    shadowElevation = 10.dp,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF81C784).copy(alpha = 0.85f)),
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = barScale
                            scaleY = barScale
                        }
                        .pointerInput(onDrag, languageCode) {
                            val touchSlop = viewConfiguration.touchSlop
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                isBarPressed = true
                                NttHaptics.performTick(context, haptics)
                                val startTime = System.currentTimeMillis()
                                var hasMovedBeyondSlop = false
                                var accumulatedDrag = Offset.Zero
                                val pointerId = down.id

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == pointerId }
                                    if (change == null) {
                                        break
                                    }

                                    if (change.changedToUp()) {
                                        isBarPressed = false
                                        val elapsed = System.currentTimeMillis() - startTime
                                        // Immediate tap response: if released without significant dragging or quick tap
                                        if (!hasMovedBeyondSlop || (elapsed < 400 && accumulatedDrag.getDistance() < touchSlop * 1.5f)) {
                                            change.consume()
                                            NttHaptics.performClick(context, haptics)
                                            OverlayStateHolder.openPanel(languageCode)
                                        }
                                        break
                                    }

                                    val delta = change.positionChange()
                                    accumulatedDrag += delta
                                    if (!hasMovedBeyondSlop && accumulatedDrag.getDistance() > touchSlop) {
                                        hasMovedBeyondSlop = true
                                    }

                                    if (hasMovedBeyondSlop && onDrag != null) {
                                        change.consume()
                                        OverlayStateHolder.onUserInteracted()
                                        onDrag(delta.x, delta.y)
                                    }
                                }
                                isBarPressed = false
                            }
                        }
                        .testTag("floating_ntt_shortcut_bar")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // NTT Logo
                        NttLogoSymbol(size = 30.dp, isCircular = true)

                        Spacer(modifier = Modifier.width(7.dp))

                        // App Name
                        Text(
                            text = overlayState.currentForegroundAppName ?: "NTT",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Detection Badge Pill (Compact summary without covering screen)
                        // Shows numbers ONLY when genuine, verified detection results exist on screen
                        val verifiedCount = liveHighlights.size
                        if (verifiedCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(vertical = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFD54F))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$verifiedCount detected",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.padding(vertical = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFA5D6A7))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Tap to scan",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Expanded Glass Panel with Screen Detected Numbers & Mic Section
            AnimatedVisibility(
                visible = overlayState.isPanelExpanded,
                enter = fadeIn(animationSpec = tween(durationMillis = 120)) +
                        scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessHigh
                            ),
                            initialScale = 0.94f
                        ),
                exit = fadeOut(animationSpec = tween(durationMillis = 120)) +
                        scaleOut(
                            animationSpec = tween(durationMillis = 120),
                            targetScale = 0.94f
                        )
            ) {
                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("overlay_glass_panel"),
                    shape = RoundedCornerShape(24.dp),
                    elevation = 14.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Header Row: Title, Rescan, and Close
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                NttLogoSymbol(size = 28.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = overlayState.currentForegroundAppName ?: "Screen Numbers",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalText
                                    )
                                    Text(
                                        text = if (liveHighlights.isNotEmpty())
                                            "🎯 ${liveHighlights.size} numbers detected on screen"
                                        else
                                            "Tap numbers or mic to understand",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (liveHighlights.isNotEmpty()) SagePrimary else MutedText,
                                        fontWeight = if (liveHighlights.isNotEmpty()) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        NttHaptics.performTick(context, haptics)
                                        NttAccessibilityService.instance?.scanAndHighlightScreenRealtime(languageCode)
                                        OverlayStateHolder.scanActiveScreen(languageCode)
                                        OverlayStateHolder.cycleDetectedResult()
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("btn_rescan_screen")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Rescan screen",
                                        tint = SagePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        NttHaptics.performTick(context, haptics)
                                        OverlayStateHolder.closePanel()
                                        onClose?.invoke()
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("btn_close_overlay")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close overlay",
                                        tint = CharcoalMedium,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Removed duplicate expanded screen-detection card; results remain in the main result card below.
                        Spacer(modifier = Modifier.height(10.dp))

                        // Mode Selector Tab: [Voice Mic] and [Type / Screen Scan]
                        TabRow(
                            selectedTabIndex = selectedOverlayMode,
                            containerColor = IvoryBackground,
                            contentColor = SagePrimary,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedOverlayMode]),
                                    color = SagePrimary,
                                    height = 3.dp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Tab(
                                selected = selectedOverlayMode == 0,
                                onClick = { selectedOverlayMode = 0 },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = null,
                                            tint = if (selectedOverlayMode == 0) SagePrimary else MutedText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Voice Mic",
                                            fontWeight = if (selectedOverlayMode == 0) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            )

                            Tab(
                                selected = selectedOverlayMode == 1,
                                onClick = { selectedOverlayMode = 1 },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Keyboard,
                                            contentDescription = null,
                                            tint = if (selectedOverlayMode == 1) SagePrimary else MutedText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Type / Scan",
                                            fontWeight = if (selectedOverlayMode == 1) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // SECTION 1: PROMINENT MIC SECTION
                        if (selectedOverlayMode == 0) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (voiceState is VoiceState.Listening) WarmAmberContainer.copy(alpha = 0.45f) else SagePrimaryContainer.copy(alpha = 0.35f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (voiceState is VoiceState.Listening) WarmAmber else SubtleBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Big Pulsing Mic Button
                                    Box(contentAlignment = Alignment.Center) {
                                        if (voiceState is VoiceState.Listening) {
                                            Box(
                                                modifier = Modifier
                                                    .size(68.dp)
                                                    .scale(pulseScale)
                                                    .clip(CircleShape)
                                                    .background(WarmAmber.copy(alpha = 0.25f))
                                            )
                                        }

                                        Surface(
                                            shape = CircleShape,
                                            color = if (voiceState is VoiceState.Listening) WarmAmber else SagePrimary,
                                            shadowElevation = 6.dp,
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clickable {
                                                    NttHaptics.performClick(context, haptics)
                                                    if (voiceState is VoiceState.Listening) {
                                                        voiceRecognizer.stopListening()
                                                    } else {
                                                        voiceRecognizer.startListening("$languageCode-IN")
                                                    }
                                                }
                                                .testTag("btn_overlay_mic_trigger")
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = if (voiceState is VoiceState.Listening) Icons.Default.Hearing else Icons.Default.Mic,
                                                    contentDescription = "Voice Input",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Spoken Status text
                                    Text(
                                        text = when (voiceState) {
                                            is VoiceState.Listening -> "Listening... Speak number or phrase now"
                                            is VoiceState.Processing -> "Understanding spoken words..."
                                            is VoiceState.Success -> "Heard: \"${(voiceState as VoiceState.Success).recognizedText}\""
                                            is VoiceState.Error -> (voiceState as VoiceState.Error).message
                                            is VoiceState.Idle -> "Tap mic to speak (e.g. 'pachpan', '₹350', 'dedh sau')"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (voiceState is VoiceState.Listening) WarmAmber else CharcoalText,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Quick Spoken Example Chips
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        listOf("pachpan", "dedh sau", "athasi", "₹500").forEach { sample ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color.White.copy(alpha = 0.8f),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
                                                modifier = Modifier.clickable {
                                                    NttHaptics.performTick(context, haptics)
                                                    OverlayStateHolder.onRealtimeQueryChanged(sample, languageCode)
                                                }
                                            ) {
                                                Text(
                                                    text = sample,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = SagePrimary,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // SECTION 2: TYPE INPUT / SCREEN DETECTED NUMBERS
                        if (selectedOverlayMode == 1) {
                            OutlinedTextField(
                                value = overlayState.currentTypedQuery,
                                onValueChange = { newQuery ->
                                    OverlayStateHolder.onRealtimeQueryChanged(newQuery, languageCode)
                                },
                                placeholder = {
                                    Text(
                                        text = "Type number or word (e.g. 78, pachpan, ₹500)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedText
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = SagePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (overlayState.currentTypedQuery.isNotBlank()) {
                                        IconButton(
                                            onClick = {
                                                NttHaptics.performTick(context, haptics)
                                                OverlayStateHolder.clearTypedInput(languageCode)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear query",
                                                tint = CharcoalMedium,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SagePrimary,
                                    unfocusedBorderColor = SubtleBorder,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = IvoryBackground,
                                    focusedTextColor = CharcoalText,
                                    unfocusedTextColor = CharcoalText
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_overlay_realtime")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Detected screen numbers if available
                            if (overlayState.detectedItems.isNotEmpty()) {
                                Text(
                                    text = "Detected on Screen (${overlayState.detectedItems.size}):",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MutedText,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    overlayState.detectedItems.forEachIndexed { index, item ->
                                        val isSelected = index == overlayState.selectedItemIndex && overlayState.currentTypedQuery.isBlank()
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                NttHaptics.performTick(context, haptics)
                                                OverlayStateHolder.selectItem(item, index)
                                            },
                                            label = {
                                                Text(
                                                    text = item.label,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = SagePrimary,
                                                selectedLabelColor = Color.White,
                                                containerColor = SagePrimaryContainer.copy(alpha = 0.65f),
                                                labelColor = CharcoalText
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        // RESULT CARD VIEW
                        val activeRes = overlayState.activeResult
                        if (activeRes != null) {
                            ResultCardView(
                                result = activeRes,
                                onSpeak = {
                                    NttHaptics.performClick(context, haptics)
                                    val speechText = NttTtsManager.buildIdentifiedNumberSpeech(activeRes)
                                    onSpeak?.invoke(speechText)
                                }
                            )
                        } else if (overlayState.currentTypedQuery.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = IvoryBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No number recognized for \"${overlayState.currentTypedQuery}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CharcoalText
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Try speaking or typing words like 'pachpan', 'dedh sau', or digits '78'.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MutedText,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Full Search Screen Link
                        if (onOpenSearch != null) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = IvoryBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .tactileClickable {
                                        NttHaptics.performClick(context, haptics)
                                        onOpenSearch()
                                    }
                                    .testTag("btn_overlay_quick_search")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = SagePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Open full voice & regional dictionary...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
