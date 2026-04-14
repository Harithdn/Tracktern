package com.internshiptracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Brand palette — deep indigo + teal accent ────────────────────────────────
// Dynamic color is intentionally DISABLED: it can pull wallpaper colours that
// clash with our fixed status colours and cause white-on-white text issues.

val Brand900  = Color(0xFF0D1B4B)   // darkest navy — hero banner start
val Brand700  = Color(0xFF1A3A8F)   // mid navy
val Brand500  = Color(0xFF2563EB)   // primary blue
val Brand300  = Color(0xFF93C5FD)   // light blue (dark-mode primary)
val Teal500   = Color(0xFF0EA5E9)   // hero banner end / accent
val Teal200   = Color(0xFF7DD3FC)   // dark-mode accent

// Status colours — high-contrast, used across every screen
val StatusApplied   = Color(0xFF2563EB)   // blue
val StatusInterview = Color(0xFFD97706)   // amber
val StatusOffer     = Color(0xFF16A34A)   // green
val StatusRejected  = Color(0xFFDC2626)   // red

// Neutral surfaces
val Surface100 = Color(0xFFF8FAFF)   // very light blue-white page background
val Surface200 = Color(0xFFEEF2FF)   // card background in light mode

private val LightColorScheme = lightColorScheme(
    primary            = Brand500,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFDBEAFE),
    onPrimaryContainer = Brand900,
    secondary          = Teal500,
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0C4A6E),
    tertiary           = Color(0xFF7C3AED),
    onTertiary         = Color.White,
    background         = Surface100,
    onBackground       = Color(0xFF0F172A),
    surface            = Color.White,
    onSurface          = Color(0xFF0F172A),
    surfaceVariant     = Surface200,
    onSurfaceVariant   = Color(0xFF334155),
    outline            = Color(0xFFCBD5E1),
    error              = StatusRejected,
    onError            = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary            = Brand300,
    onPrimary          = Brand900,
    primaryContainer   = Brand700,
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary          = Teal200,
    onSecondary        = Color(0xFF0C4A6E),
    secondaryContainer = Color(0xFF0369A1),
    onSecondaryContainer = Color(0xFFE0F2FE),
    tertiary           = Color(0xFFC4B5FD),
    onTertiary         = Color(0xFF4C1D95),
    background         = Color(0xFF0F172A),
    onBackground       = Color(0xFFE2E8F0),
    surface            = Color(0xFF1E293B),
    onSurface          = Color(0xFFE2E8F0),
    surfaceVariant     = Color(0xFF1E3A5F),
    onSurfaceVariant   = Color(0xFF94A3B8),
    outline            = Color(0xFF334155),
    error              = Color(0xFFFCA5A5),
    onError            = Color(0xFF7F1D1D)
)

/**
 * App-level Material 3 theme.
 * Dynamic colour is OFF so our status colours never get overridden.
 */
@Composable
fun InternshipTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
