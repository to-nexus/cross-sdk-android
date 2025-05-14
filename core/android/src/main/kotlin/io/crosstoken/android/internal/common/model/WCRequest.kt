package io.crosstoken.android.internal.common.model

import io.crosstoken.android.internal.common.model.type.ClientParams
import io.crosstoken.foundation.common.model.Topic
import io.crosstoken.utils.Empty

data class WCRequest(
    val topic: Topic,
    val id: Long,
    val method: String,
    val params: ClientParams,
    val message: String = String.Empty,
    val publishedAt: Long = 0,
    val encryptedMessage: String = String.Empty,
    val attestation: String? = null,
    val transportType: TransportType
)