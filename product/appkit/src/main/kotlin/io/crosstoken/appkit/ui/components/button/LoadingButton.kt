package io.crosstoken.appkit.ui.components.button

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.crosstoken.appkit.ui.components.internal.commons.LoadingSpinner
import io.crosstoken.appkit.ui.components.internal.commons.button.ButtonSize
import io.crosstoken.appkit.ui.components.internal.commons.button.ButtonStyle
import io.crosstoken.appkit.ui.components.internal.commons.button.StyledButton
import io.crosstoken.appkit.ui.previews.ComponentPreview
import io.crosstoken.appkit.ui.previews.UiModePreview
import io.crosstoken.appkit.ui.theme.ProvideAppKitThemeComposition

@Composable
internal fun LoadingButton() {
    ProvideAppKitThemeComposition {
        StyledButton(style = ButtonStyle.ACCOUNT, size = ButtonSize.M, onClick = {}) {
            Box(
                modifier = Modifier.width(100.dp), contentAlignment = Alignment.Center
            ) {
                LoadingSpinner(size = 16.dp, strokeWidth = 1.dp)
            }
        }
    }
}

@UiModePreview
@Composable
private fun LoadingButtonPreview() {
    ComponentPreview {
        LoadingButton()
    }
}
