package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speech.LocalNttTtsManager
import com.example.speech.NttTtsManager
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryContainer
import com.example.ui.theme.SubtleBorder
import com.example.ui.util.NttHaptics

/**
 * A dedicated, polished voice button for reading back identified numbers, prices,
 * or word meanings using the Android TextToSpeech engine.
 * Displays animated sound waves during playback and allows instant pause/stop on tap.
 */
@Composable
fun ListenSpeakerButton(
    text: String,
    modifier: Modifier = Modifier,
    spokenText: String? = null,
    languageTag: String? = null,
    showLabel: Boolean = false,
    labelText: String = "Listen",
    size: Dp = 38.dp,
    tint: Color = SagePrimary,
    containerColor: Color? = null,
    ttsManager: NttTtsManager? = null,
    onSpeakFallback: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val resolvedTts = ttsManager ?: LocalNttTtsManager.current ?: remember { NttTtsManager.getInstance(context) }

    val currentlyPlayingText by resolvedTts.currentlyPlayingText.collectAsState()
    val isPlayingOverall by resolvedTts.isPlaying.collectAsState()

    val trimmedText = text.trim()
    val actualTextToSpeak = spokenText?.trim()?.ifEmpty { null } ?: trimmedText
    val isPlayingThis = isPlayingOverall && (currentlyPlayingText == actualTextToSpeak || currentlyPlayingText == trimmedText)

    val resolvedTag = remember(languageTag) {
        if (!languageTag.isNullOrBlank()) {
            if (languageTag.contains("-")) languageTag else "${languageTag}-IN"
        } else {
            "hi-IN"
        }
    }

    // Animation transition for playing state
    val infiniteTransition = rememberInfiniteTransition(label = "audio_wave_transition")

    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 13f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )

    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 11f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(320, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )

    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isPlayingThis) {
            SagePrimaryContainer
        } else {
            containerColor ?: Color.Transparent
        },
        animationSpec = tween(250),
        label = "bg_color"
    )

    val actionDescription = if (isPlayingThis) {
        "Stop listening"
    } else {
        "Listen to $actualTextToSpeak"
    }

    val sanitizedTag = trimmedText.take(16).replace("[^a-zA-Z0-9_]".toRegex(), "_")

    if (showLabel) {
        // Pill-style Listen button with text label
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = bgColor,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isPlayingThis) tint else SubtleBorder
            ),
            modifier = modifier
                .defaultMinSize(minHeight = 40.dp)
                .scale(if (isPlayingThis) pulseScale else 1.0f)
                .clip(RoundedCornerShape(12.dp))
                .clickable(
                    role = Role.Button
                ) {
                    NttHaptics.performClick(context, haptics)
                    if (isPlayingThis) {
                        resolvedTts.stop()
                    } else {
                        if (onSpeakFallback != null && !isPlayingOverall) {
                            onSpeakFallback(actualTextToSpeak)
                        } else {
                            resolvedTts.toggleSpeak(actualTextToSpeak, resolvedTag)
                        }
                    }
                }
                .semantics {
                    contentDescription = actionDescription
                    role = Role.Button
                }
                .testTag("btn_listen_pill_$sanitizedTag")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isPlayingThis) {
                    SoundWaveEqualizer(
                        bar1 = bar1Height,
                        bar2 = bar2Height,
                        bar3 = bar3Height,
                        color = tint
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Playing...",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                        fontWeight = FontWeight.Bold,
                        color = tint
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = labelText,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = tint
                    )
                }
            }
        }
    } else {
        // Compact round speaker button with sound waves
        Box(
            modifier = modifier
                .size(maxOf(size, 48.dp)) // Ensuring minimum interactive touch target
                .semantics {
                    contentDescription = actionDescription
                    role = Role.Button
                }
                .testTag("btn_listen_round_$sanitizedTag"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(if (isPlayingThis) pulseScale else 1.0f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button
                    ) {
                        NttHaptics.performClick(context, haptics)
                        if (isPlayingThis) {
                            resolvedTts.stop()
                        } else {
                            if (onSpeakFallback != null && !isPlayingOverall) {
                                onSpeakFallback(actualTextToSpeak)
                            } else {
                                resolvedTts.toggleSpeak(actualTextToSpeak, resolvedTag)
                            }
                        }
                    }
                    .clip(CircleShape)
                    .background(bgColor)
                    .then(
                        if (isPlayingThis) {
                            Modifier.border(1.2.dp, tint.copy(alpha = 0.6f), CircleShape)
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isPlayingThis) {
                    SoundWaveEqualizer(
                        bar1 = bar1Height,
                        bar2 = bar2Height,
                        bar3 = bar3Height,
                        color = tint
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(size * 0.55f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SoundWaveEqualizer(
    bar1: Float,
    bar2: Float,
    bar3: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(bar1.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(bar2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(bar3.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(color)
        )
    }
}
