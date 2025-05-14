package io.crosstoken.sign.json_rpc.domain

import io.crosstoken.android.internal.common.json_rpc.data.JsonRpcSerializer
import io.crosstoken.android.internal.common.storage.rpc.JsonRpcHistory
import io.crosstoken.foundation.common.model.Topic
import io.crosstoken.sign.common.model.Request
import io.crosstoken.sign.common.model.vo.clientsync.session.SignRpc
import io.crosstoken.sign.json_rpc.model.JsonRpcMethod
import io.crosstoken.sign.json_rpc.model.toRequest
import kotlinx.coroutines.supervisorScope

internal class GetPendingRequestsUseCaseByTopic(
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) : GetPendingRequestsUseCaseByTopicInterface {

    override suspend fun getPendingRequests(topic: Topic): List<Request<String>> = supervisorScope {
        jsonRpcHistory.getListOfPendingRecordsByTopic(topic)
            .filter { record -> record.method == JsonRpcMethod.WC_SESSION_REQUEST }
            .mapNotNull { record -> serializer.tryDeserialize<SignRpc.SessionRequest>(record.body)?.toRequest(record) }
    }
}

internal interface GetPendingRequestsUseCaseByTopicInterface {
    suspend fun getPendingRequests(topic: Topic): List<Request<String>>
}