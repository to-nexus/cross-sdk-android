package io.crosstoken.appkit.ui.components.button

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import io.crosstoken.appkit.ui.components.internal.commons.LoadingSpinner
import io.crosstoken.appkit.ui.components.internal.commons.button.ButtonSize
import io.crosstoken.appkit.ui.components.internal.commons.button.ButtonStyle
import io.crosstoken.appkit.ui.components.internal.commons.button.ImageButton
import io.crosstoken.appkit.ui.components.internal.commons.button.TextButton
import io.crosstoken.appkit.ui.previews.MultipleComponentsPreview
import io.crosstoken.appkit.ui.previews.UiModePreview
import io.crosstoken.appkit.ui.theme.ProvideAppKitThemeComposition

enum class ConnectButtonSize {
    NORMAL, SMALL
}

@Composable
fun ConnectButton(
    state: AppKitState,
    buttonSize: ConnectButtonSize = ConnectButtonSize.NORMAL
) {
    val isLoading: Boolean by state.isOpen.collectAsState(initial = false)
    val isConnected: Boolean by state.isConnected.collectAsState(initial = false)

    ConnectButton(
        size = buttonSize,
        isLoading = isLoading,
        isEnabled = !isConnected
    ) {
        state.openAppKit()
    }
}

@Composable
internal fun ConnectButton(
    size: ConnectButtonSize,
    isLoading: Boolean = false,
    isEnabled: Boolean = true,
    onClick: () -> Unit,
) {
    ProvideAppKitThemeComposition {
        val buttonSize = when (size) {
            ConnectButtonSize.NORMAL -> ButtonSize.M
            ConnectButtonSize.SMALL -> ButtonSize.S
        }
        if (isLoading && isEnabled) {
            ImageButton(
                text = "Connecting...",
                image = { LoadingSpinner(size = 10.dp, strokeWidth = 2.dp) },
                style = ButtonStyle.LOADING,
                size = buttonSize
            ) {}
        } else {
            val text = when (size) {
                ConnectButtonSize.NORMAL -> "Connect wallet"
                ConnectButtonSize.SMALL -> "Connect"
            }
            TextButton(
                text = text,
                style = ButtonStyle.MAIN,
                size = buttonSize,
                isEnabled = isEnabled,
                onClick = onClick
            )
        }
    }
}

@UiModePreview
@Composable
private fun ConnectButtonPreview() {
    MultipleComponentsPreview(
        { ConnectButton(size = ConnectButtonSize.NORMAL) {} },
        { ConnectButton(size = ConnectButtonSize.SMALL) {} },
    )
}

@UiModePreview
@Composable
private fun DisabledConnectButtonPreview() {
    MultipleComponentsPreview(
        { ConnectButton(size = ConnectButtonSize.NORMAL, isEnabled = false) {} },
        { ConnectButton(size = ConnectButtonSize.SMALL, isEnabled = false) {} },
    )
}

@UiModePreview
@Composable
private fun LoadingConnectButtonPreview() {
    MultipleComponentsPreview(
        { ConnectButton(size = ConnectButtonSize.NORMAL, isLoading = true) {} },
        { ConnectButton(size = ConnectButtonSize.SMALL, isLoading = true) {} },
    )
}
