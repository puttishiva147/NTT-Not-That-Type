package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SagePrimary,
    onPrimary = SageOnPrimary,
    primaryContainer = SagePrimaryContainer,
    onPrimaryContainer = SageOnPrimaryContainer,
    secondary = SageSecondary,
    onSecondary = SageOnPrimary,
    secondaryContainer = SageSecondaryContainer,
    onSecondaryContainer = SageOnSecondaryContainer,
    tertiary = WarmAmber,
    onTertiary = SageOnPrimary,
    tertiaryContainer = WarmAmberContainer,
    onTertiaryContainer = WarmAmberOnContainer,
    background = IvoryBackground,
    onBackground = CharcoalText,
    surface = IvorySurface,
    onSurface = CharcoalText,
    surfaceVariant = IvorySurfaceVariant,
    onSurfaceVariant = MutedText,
    surfaceContainer = IvorySurfaceContainer,
    surfaceContainerHigh = IvorySurfaceContainerHigh,
    outline = OutlineBorder,
    outlineVariant = SubtleBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkSagePrimary,
    onPrimary = CharcoalText,
    primaryContainer = DarkSageContainer,
    onPrimaryContainer = DarkText,
    secondary = DarkSagePrimary,
    onSecondary = CharcoalText,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkText,
    tertiary = DarkAmber,
    onTertiary = CharcoalText,
    tertiaryContainer = DarkSurfaceVariant,
    onTertiaryContainer = DarkAmber,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkMuted,
    surfaceContainer = DarkSurfaceVariant,
    surfaceContainerHigh = DarkSurface,
    outline = DarkMuted,
    outlineVariant = SubtleBorder
)

@Composable
fun NTTTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

