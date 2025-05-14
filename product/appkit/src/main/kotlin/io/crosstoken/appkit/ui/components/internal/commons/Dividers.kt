package io.crosstoken.appkit.ui.components.internal.commons

import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.crosstoken.appkit.ui.theme.AppKitTheme

@Composable
internal fun FullWidthDivider(modifier: Modifier = Modifier) {
    Divider(color = AppKitTheme.colors.grayGlass05, modifier = modifier)
}
