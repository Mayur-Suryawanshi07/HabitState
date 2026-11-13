package com.mayur.habitstate.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ModernDarkPrimary,
    onPrimary = ModernDarkOnPrimary,
    primaryContainer = ModernDarkPrimaryContainer,
    onPrimaryContainer = ModernDarkOnPrimaryContainer,
    inversePrimary = ModernDarkInversePrimary,
    secondary = ModernDarkSecondary,
    onSecondary = ModernDarkOnSecondary,
    secondaryContainer = ModernDarkSecondaryContainer,
    onSecondaryContainer = ModernDarkOnSecondaryContainer,
    tertiary = ModernDarkTertiary,
    onTertiary = ModernDarkOnTertiary,
    tertiaryContainer = ModernDarkTertiaryContainer,
    onTertiaryContainer = ModernDarkOnTertiaryContainer,
    background = ModernDarkBackground,
    onBackground = ModernDarkOnBackground,
    surface = ModernDarkSurface,
    onSurface = ModernDarkOnSurface,
    surfaceVariant = ModernDarkSurfaceVariant,
    onSurfaceVariant = ModernDarkOnSurfaceVariant,
    surfaceTint = ModernDarkPrimary,
    inverseSurface = ModernDarkInverseSurface,
    inverseOnSurface = ModernDarkInverseOnSurface,
    outline = ModernDarkOutline,
    outlineVariant = ModernDarkOutlineVariant,
    scrim = Color.Black,
    surfaceBright = ModernDarkSurfaceBright,
    surfaceDim = ModernDarkSurfaceDim,
    surfaceContainer = ModernDarkSurfaceContainer,
    surfaceContainerHigh = ModernDarkSurfaceContainerHigh,
    surfaceContainerHighest = ModernDarkSurfaceContainerHighest,
    surfaceContainerLow = ModernDarkSurfaceContainerLow,
    surfaceContainerLowest = ModernDarkSurfaceContainerLowest
)

private val LightColorScheme = lightColorScheme(
    primary = PurpleFocusPrimary,
    onPrimary = PurpleFocusOnPrimary,
    primaryContainer = PurpleFocusPrimaryContainer,
    onPrimaryContainer = PurpleFocusOnPrimaryContainer,
    inversePrimary = PurpleFocusInversePrimary,
    secondary = PurpleFocusSecondary,
    onSecondary = PurpleFocusOnSecondary,
    secondaryContainer = PurpleFocusSecondaryContainer,
    onSecondaryContainer = PurpleFocusOnSecondaryContainer,
    tertiary = PurpleFocusTertiary,
    onTertiary = PurpleFocusOnTertiary,
    tertiaryContainer = PurpleFocusTertiaryContainer,
    onTertiaryContainer = PurpleFocusOnTertiaryContainer,
    background = PurpleFocusBackground,
    onBackground = PurpleFocusOnBackground,
    surface = PurpleFocusSurface,
    onSurface = PurpleFocusOnSurface,
    surfaceVariant = PurpleFocusSurfaceVariant,
    onSurfaceVariant = PurpleFocusOnSurfaceVariant,
    surfaceTint = PurpleFocusPrimary,
    inverseSurface = PurpleFocusInverseSurface,
    inverseOnSurface = PurpleFocusInverseOnSurface,
    outline = PurpleFocusOutline,
    outlineVariant = PurpleFocusOutlineVariant,
    scrim = Color.Black,
    surfaceBright = PurpleFocusSurfaceBright,
    surfaceDim = PurpleFocusSurfaceDim,
    surfaceContainer = PurpleFocusSurfaceContainer,
    surfaceContainerHigh = PurpleFocusSurfaceContainerHigh,
    surfaceContainerHighest = PurpleFocusSurfaceContainerHighest,
    surfaceContainerLow = PurpleFocusSurfaceContainerLow,
    surfaceContainerLowest = PurpleFocusSurfaceContainerLowest
)

@Composable
fun HabitStateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Brand palettes take precedence over Android 12+ dynamic wallpaper colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(activity.window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = animateColorScheme(colorScheme),
        typography = Typography,
        content = content
    )
}

@Composable
private fun animateColorScheme(target: ColorScheme): ColorScheme {
    val spec = tween<Color>(durationMillis = 300)
    val primary by animateColorAsState(target.primary, spec, label = "primary")
    val onPrimary by animateColorAsState(target.onPrimary, spec, label = "onPrimary")
    val primaryContainer by animateColorAsState(target.primaryContainer, spec, label = "primaryContainer")
    val onPrimaryContainer by animateColorAsState(target.onPrimaryContainer, spec, label = "onPrimaryContainer")
    val inversePrimary by animateColorAsState(target.inversePrimary, spec, label = "inversePrimary")
    val secondary by animateColorAsState(target.secondary, spec, label = "secondary")
    val onSecondary by animateColorAsState(target.onSecondary, spec, label = "onSecondary")
    val secondaryContainer by animateColorAsState(target.secondaryContainer, spec, label = "secondaryContainer")
    val onSecondaryContainer by animateColorAsState(target.onSecondaryContainer, spec, label = "onSecondaryContainer")
    val tertiary by animateColorAsState(target.tertiary, spec, label = "tertiary")
    val onTertiary by animateColorAsState(target.onTertiary, spec, label = "onTertiary")
    val tertiaryContainer by animateColorAsState(target.tertiaryContainer, spec, label = "tertiaryContainer")
    val onTertiaryContainer by animateColorAsState(target.onTertiaryContainer, spec, label = "onTertiaryContainer")
    val background by animateColorAsState(target.background, spec, label = "background")
    val onBackground by animateColorAsState(target.onBackground, spec, label = "onBackground")
    val surface by animateColorAsState(target.surface, spec, label = "surface")
    val onSurface by animateColorAsState(target.onSurface, spec, label = "onSurface")
    val surfaceVariant by animateColorAsState(target.surfaceVariant, spec, label = "surfaceVariant")
    val onSurfaceVariant by animateColorAsState(target.onSurfaceVariant, spec, label = "onSurfaceVariant")
    val surfaceTint by animateColorAsState(target.surfaceTint, spec, label = "surfaceTint")
    val inverseSurface by animateColorAsState(target.inverseSurface, spec, label = "inverseSurface")
    val inverseOnSurface by animateColorAsState(target.inverseOnSurface, spec, label = "inverseOnSurface")
    val outline by animateColorAsState(target.outline, spec, label = "outline")
    val outlineVariant by animateColorAsState(target.outlineVariant, spec, label = "outlineVariant")
    val surfaceBright by animateColorAsState(target.surfaceBright, spec, label = "surfaceBright")
    val surfaceDim by animateColorAsState(target.surfaceDim, spec, label = "surfaceDim")
    val surfaceContainer by animateColorAsState(target.surfaceContainer, spec, label = "surfaceContainer")
    val surfaceContainerHigh by animateColorAsState(target.surfaceContainerHigh, spec, label = "surfaceContainerHigh")
    val surfaceContainerHighest by animateColorAsState(target.surfaceContainerHighest, spec, label = "surfaceContainerHighest")
    val surfaceContainerLow by animateColorAsState(target.surfaceContainerLow, spec, label = "surfaceContainerLow")
    val surfaceContainerLowest by animateColorAsState(target.surfaceContainerLowest, spec, label = "surfaceContainerLowest")

    return target.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        inversePrimary = inversePrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceTint = surfaceTint,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        outline = outline,
        outlineVariant = outlineVariant,
        surfaceBright = surfaceBright,
        surfaceDim = surfaceDim,
        surfaceContainer = surfaceContainer,
        surfaceContainerHigh = surfaceContainerHigh,
        surfaceContainerHighest = surfaceContainerHighest,
        surfaceContainerLow = surfaceContainerLow,
        surfaceContainerLowest = surfaceContainerLowest
    )
}
