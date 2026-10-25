package com.example.ui.util

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

object NttHaptics {

    fun performClick(context: Context?, hapticFeedback: HapticFeedback? = null) {
        hapticFeedback?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        try {
            if (context != null) {
                val vibrator = getVibrator(context)
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(15)
                    }
                }
            }
        } catch (_: Exception) {
            hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun performTick(context: Context?, hapticFeedback: HapticFeedback? = null) {
        try {
            if (context != null) {
                val vibrator = getVibrator(context)
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(8)
                    }
                }
            }
        } catch (_: Exception) {
            hapticFeedback?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    fun performHeavy(context: Context?, hapticFeedback: HapticFeedback? = null) {
        hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
        try {
            if (context != null) {
                val vibrator = getVibrator(context)
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(35)
                    }
                }
            }
        } catch (_: Exception) {
            hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun performSuccess(context: Context?, hapticFeedback: HapticFeedback? = null) {
        try {
            if (context != null) {
                val vibrator = getVibrator(context)
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val timings = longArrayOf(0, 18, 50, 25)
                        val amplitudes = intArrayOf(0, 160, 0, 255)
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(30)
                    }
                }
            }
        } catch (_: Exception) {
            hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}

/**
 * Modern spring tactile bounce modifier with automatic haptic feedback on press and release.
 */
fun Modifier.tactileClickable(
    enabled: Boolean = true,
    scaleDown: Float = 0.94f,
    onClick: () -> Unit
): Modifier = composed {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tactile_scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            while (true) {
                awaitPointerEventScope {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    NttHaptics.performTick(context, haptics)
                    val up = waitForUpOrCancellation()
                    isPressed = false
                    if (up != null) {
                        NttHaptics.performClick(context, haptics)
                        onClick()
                    }
                }
            }
        }
}
