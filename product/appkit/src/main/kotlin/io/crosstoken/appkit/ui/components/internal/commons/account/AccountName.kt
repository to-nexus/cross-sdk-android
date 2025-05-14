package io.crosstoken.appkit.ui.components.internal.commons.account

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import io.crosstoken.modal.ui.components.common.roundedClickable
import io.crosstoken.appkit.domain.model.AccountData
import io.crosstoken.appkit.domain.model.Identity
import io.crosstoken.appkit.ui.components.internal.commons.CopyIcon
import io.crosstoken.appkit.ui.previews.ComponentPreview
import io.crosstoken.appkit.ui.previews.UiModePreview
import io.crosstoken.appkit.ui.previews.accountDataPreview
import io.crosstoken.appkit.ui.theme.AppKitTheme
import io.crosstoken.appkit.utils.toVisibleAddress

@Composable
internal fun AccountName(accountData: AccountData) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        val clipboardManager: ClipboardManager = LocalClipboardManager.current
        val name = accountData.identity?.name ?: accountData.address.toVisibleAddress()
        Text(text = name, style = AppKitTheme.typo.mediumTitle600)
        CopyIcon(
            modifier = Modifier
                .padding(10.dp)
                .size(16.dp)
                .roundedClickable { clipboardManager.setText(AnnotatedString(accountData.address)) }
        )
    }
}

@UiModePreview
@Composable
private fun AccountAddressPreview() {
    ComponentPreview {
        AccountName(accountDataPreview)
    }
}

@UiModePreview
@Composable
private fun AccountNamePreview() {
    ComponentPreview {
        AccountName(accountDataPreview.copy(identity = Identity(name = "testIdentity.eth")))
    }
}
