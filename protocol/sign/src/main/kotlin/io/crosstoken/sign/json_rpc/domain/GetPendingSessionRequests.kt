package io.crosstoken.sign.json_rpc.domain

import io.crosstoken.android.internal.common.json_rpc.data.JsonRpcSerializer
import io.crosstoken.android.internal.common.storage.rpc.JsonRpcHistory
import io.crosstoken.sign.common.model.Request
import io.crosstoken.sign.common.model.vo.clientsync.session.SignRpc
import io.crosstoken.sign.json_rpc.model.JsonRpcMethod
import io.crosstoken.sign.json_rpc.model.toRequest
import kotlinx.coroutines.supervisorScope

internal class GetPendingSessionRequests(
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) {

    suspend operator fun invoke(): List<Request<String>> = supervisorScope {
        jsonRpcHistory.getListOfPendingSessionRequests()
            .mapNotNull { record -> serializer.tryDeserialize<SignRpc.SessionRequest>(record.body)?.toRequest(record) }
    }
}