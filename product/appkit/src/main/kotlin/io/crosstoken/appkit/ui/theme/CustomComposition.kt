package io.crosstoken.appkit.ui.theme

import androidx.compose.runtime.compositionLocalOf
import io.crosstoken.appkit.ui.AppKitTheme

internal data class CustomComposition(
    val mode: AppKitTheme.Mode = AppKitTheme.Mode.AUTO,
    val lightColors: AppKitTheme.Colors = AppKitTheme.provideLightAppKitColors(),
    val darkColors: AppKitTheme.Colors = AppKitTheme.provideDarkAppKitColor(),
)

internal val LocalCustomComposition = compositionLocalOf { CustomComposition() }