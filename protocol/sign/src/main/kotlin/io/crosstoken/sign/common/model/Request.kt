package io.crosstoken.sign.common.model

import io.crosstoken.android.internal.common.model.Expiry
import io.crosstoken.android.internal.common.model.TransportType
import io.crosstoken.foundation.common.model.Topic

internal data class Request<T>(
    val id: Long,
    val topic: Topic,
    val method: String,
    val chainId: String?,
    val params: T,
    val expiry: Expiry? = null,
    val transportType: TransportType?
)