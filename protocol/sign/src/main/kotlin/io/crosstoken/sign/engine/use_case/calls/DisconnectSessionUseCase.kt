package io.crosstoken.sign.engine.use_case.calls

import io.crosstoken.android.internal.common.exception.CannotFindSequenceForTopic
import io.crosstoken.android.internal.common.exception.Reason
import io.crosstoken.android.internal.common.model.IrnParams
import io.crosstoken.android.internal.common.model.Tags
import io.crosstoken.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import io.crosstoken.android.internal.utils.dayInSeconds
import io.crosstoken.foundation.common.model.Topic
import io.crosstoken.foundation.common.model.Ttl
import io.crosstoken.foundation.util.Logger
import io.crosstoken.sign.common.exceptions.NO_SEQUENCE_FOR_TOPIC_MESSAGE
import io.crosstoken.sign.common.model.vo.clientsync.session.SignRpc
import io.crosstoken.sign.common.model.vo.clientsync.session.params.SignParams
import io.crosstoken.sign.storage.sequence.SessionStorageRepository
import kotlinx.coroutines.supervisorScope

internal class DisconnectSessionUseCase(
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val sessionStorageRepository: SessionStorageRepository,
    private val logger: Logger,
) : DisconnectSessionUseCaseInterface {
    override suspend fun disconnect(topic: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        if (!sessionStorageRepository.isSessionValid(Topic(topic))) {
            logger.error("Sending session disconnect error: invalid session $topic")
            return@supervisorScope onFailure(CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic"))
        }

        val deleteParams = SignParams.DeleteParams(Reason.UserDisconnected.code, Reason.UserDisconnected.message)
        val sessionDelete = SignRpc.SessionDelete(params = deleteParams)
        val irnParams = IrnParams(Tags.SESSION_DELETE, Ttl(dayInSeconds), correlationId = sessionDelete.id)

        logger.log("Sending session disconnect on topic: $topic")
        jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, sessionDelete,
            onSuccess = {
                logger.log("Disconnect sent successfully on topic: $topic")
                sessionStorageRepository.deleteSession(Topic(topic))
                jsonRpcInteractor.unsubscribe(Topic(topic))
                onSuccess()
            },
            onFailure = { error ->
                logger.error("Sending session disconnect error: $error on topic: $topic")
                onFailure(error)
            }
        )
    }
}

internal interface DisconnectSessionUseCaseInterface {
    suspend fun disconnect(topic: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}