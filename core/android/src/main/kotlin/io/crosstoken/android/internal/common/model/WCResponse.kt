package io.crosstoken.android.internal.common.model

import io.crosstoken.android.internal.common.JsonRpcResponse
import io.crosstoken.android.internal.common.model.type.ClientParams
import io.crosstoken.foundation.common.model.Topic

data class WCResponse(
    val topic: Topic,
    val method: String,
    val response: JsonRpcResponse,
    val params: ClientParams,
)