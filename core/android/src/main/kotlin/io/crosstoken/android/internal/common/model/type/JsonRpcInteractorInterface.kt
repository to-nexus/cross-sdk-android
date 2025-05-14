package io.crosstoken.android.internal.common.model.type

import io.crosstoken.android.internal.common.model.SDKError
import io.crosstoken.android.internal.common.model.WCRequest
import io.crosstoken.android.internal.common.model.WCResponse
import kotlinx.coroutines.flow.SharedFlow

interface JsonRpcInteractorInterface {
    val clientSyncJsonRpc: SharedFlow<WCRequest>
    val peerResponse: SharedFlow<WCResponse>
    val internalErrors: SharedFlow<SDKError>
}