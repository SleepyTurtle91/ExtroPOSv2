package com.extrotarget.extroposv2.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = StitchColor.Primary,
    onPrimary = StitchColor.OnPrimary,
    primaryContainer = StitchColor.PrimaryContainer,
    secondary = StitchColor.Secondary,
    onSecondary = StitchColor.OnPrimary,
    secondaryContainer = StitchColor.SecondaryContainer,
    tertiary = StitchColor.Tertiary,
    onTertiary = StitchColor.OnPrimary,
    tertiaryContainer = StitchColor.TertiaryContainer,
    error = StitchColor.Error,
    errorContainer = StitchColor.ErrorContainer,
    onError = StitchColor.OnPrimary,
    onErrorContainer = StitchColor.OnErrorContainer,
    background = StitchColor.Background,
    onBackground = StitchColor.OnSurface,
    surface = StitchColor.Surface,
    onSurface = StitchColor.OnSurface,
    surfaceVariant = StitchColor.SurfaceContainerHighest,
    onSurfaceVariant = StitchColor.OnSurfaceVariant,
    outline = StitchColor.Outline,
    inverseSurface = StitchColor.InverseSurface,
    inverseOnSurface = StitchColor.SurfaceBright
)

@Composable
fun ExtroPOSV2Theme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = StitchColor.Surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = StitchTypography,
        content = content
    )
}
