package io.crosstoken.android.internal.common.model

import io.crosstoken.foundation.common.model.PublicKey

data class Participants(
    val senderPublicKey: PublicKey,
    val receiverPublicKey: PublicKey,
)