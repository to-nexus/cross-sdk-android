package io.crosstoken.sign.json_rpc.domain

import io.crosstoken.android.internal.common.json_rpc.data.JsonRpcSerializer
import io.crosstoken.android.internal.common.model.AppMetaDataType
import io.crosstoken.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import io.crosstoken.android.internal.common.storage.rpc.JsonRpcHistory
import io.crosstoken.foundation.common.model.Topic
import io.crosstoken.sign.common.model.vo.clientsync.session.SignRpc
import io.crosstoken.sign.engine.model.EngineDO
import io.crosstoken.sign.engine.model.mapper.toSessionRequest
import io.crosstoken.sign.json_rpc.model.JsonRpcMethod
import io.crosstoken.sign.json_rpc.model.toRequest
import kotlinx.coroutines.supervisorScope

internal class GetPendingSessionRequestByTopicUseCase(
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
) : GetPendingSessionRequestByTopicUseCaseInterface {

    override suspend fun getPendingSessionRequests(topic: Topic): List<EngineDO.SessionRequest> = supervisorScope {
        jsonRpcHistory.getListOfPendingRecordsByTopic(topic)
            .filter { record -> record.method == JsonRpcMethod.WC_SESSION_REQUEST }
            .mapNotNull { record ->
                serializer.tryDeserialize<SignRpc.SessionRequest>(record.body)?.toRequest(record)
                    ?.toSessionRequest(metadataStorageRepository.getByTopicAndType(Topic(record.topic), AppMetaDataType.PEER))
            }
    }
}

internal interface GetPendingSessionRequestByTopicUseCaseInterface {
    suspend fun getPendingSessionRequests(topic: Topic): List<EngineDO.SessionRequest>
}