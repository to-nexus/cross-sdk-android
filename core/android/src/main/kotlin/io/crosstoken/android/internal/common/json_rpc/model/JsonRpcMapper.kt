@file:JvmSynthetic

package io.crosstoken.android.internal.common.json_rpc.model

import io.crosstoken.android.internal.common.JsonRpcResponse
import io.crosstoken.android.internal.common.json_rpc.domain.relay.Subscription
import io.crosstoken.android.internal.common.model.IrnParams
import io.crosstoken.android.internal.common.model.TransportType
import io.crosstoken.android.internal.common.model.WCRequest
import io.crosstoken.android.internal.common.model.WCResponse
import io.crosstoken.android.internal.common.model.sync.ClientJsonRpc
import io.crosstoken.android.internal.common.model.type.ClientParams
import io.crosstoken.foundation.common.model.Topic
import io.crosstoken.foundation.network.model.Relay

@JvmSynthetic
internal fun JsonRpcHistoryRecord.toWCResponse(result: JsonRpcResponse, params: ClientParams): WCResponse =
    WCResponse(Topic(topic), method, result, params)

@JvmSynthetic
internal fun IrnParams.toRelay(): Relay.Model.IrnParams =
    Relay.Model.IrnParams(tag.id, ttl.seconds, correlationId, rpcMethods, chainId, txHashes, contractAddresses, prompt)

internal fun Subscription.toWCRequest(clientJsonRpc: ClientJsonRpc, params: ClientParams, transportType: TransportType): WCRequest =
    WCRequest(topic, clientJsonRpc.id, clientJsonRpc.method, params, decryptedMessage, publishedAt, encryptedMessage, attestation, transportType)