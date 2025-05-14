package io.crosstoken.appkit.utils

import io.crosstoken.appkit.ui.components.button.AccountButtonType
import io.crosstoken.appkit.ui.components.button.ConnectButtonSize

internal fun Int.toConnectButtonSize() = when (this) {
    1 -> ConnectButtonSize.SMALL
    else -> ConnectButtonSize.NORMAL
}

internal fun Int.toAccountButtonType() = when(this) {
    1 -> AccountButtonType.MIXED
    else -> AccountButtonType.NORMAL
}
