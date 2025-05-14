package io.crosstoken.appkit.ui.components.internal.walletconnect

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import io.crosstoken.appkit.ui.components.internal.commons.ListSelectRow
import io.crosstoken.appkit.ui.components.internal.commons.AllWalletsIcon
import io.crosstoken.appkit.ui.components.internal.commons.TextLabel
import io.crosstoken.appkit.ui.previews.MultipleComponentsPreview
import io.crosstoken.appkit.ui.previews.UiModePreview

fun LazyListScope.allWallets(text: String, isEnabled: Boolean = true, onClick: () -> Unit) {
    item { WalletConnectAll(text = text, isEnabled = isEnabled, onClick = onClick) }
}

@Composable
private fun WalletConnectAll(text: String, isEnabled: Boolean = true, onClick: () -> Unit) {
    ListSelectRow(
        startIcon = { AllWalletsIcon() },
        text = "All wallets",
        label = { TextLabel(text = text, isEnabled = isEnabled) },
        onClick = onClick,
        contentPadding = PaddingValues(vertical = 4.dp)
    )
}

@UiModePreview
@Composable
private fun WalletConnectListSelectPreview() {
    MultipleComponentsPreview(
        { WalletConnectAll(text = "240+") {} },
        { WalletConnectAll(text = "240+", isEnabled = false) {} },
    )
}