package io.crosstoken.android.internal.common.json_rpc.model

import io.crosstoken.android.internal.common.model.TransportType

data class JsonRpcHistoryRecord(
    val id: Long,
    val topic: String,
    val method: String,
    val body: String,
    val response: String?,
    val transportType: TransportType?
)
