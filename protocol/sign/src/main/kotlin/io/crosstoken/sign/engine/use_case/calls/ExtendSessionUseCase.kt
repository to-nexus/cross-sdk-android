package io.crosstoken.sign.engine.use_case.calls

import io.crosstoken.android.internal.common.exception.CannotFindSequenceForTopic
import io.crosstoken.android.internal.common.model.IrnParams
import io.crosstoken.android.internal.common.model.Tags
import io.crosstoken.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import io.crosstoken.android.internal.utils.dayInSeconds
import io.crosstoken.android.internal.utils.weekInSeconds
import io.crosstoken.foundation.common.model.Topic
import io.crosstoken.foundation.common.model.Ttl
import io.crosstoken.foundation.util.Logger
import io.crosstoken.sign.common.exceptions.NO_SEQUENCE_FOR_TOPIC_MESSAGE
import io.crosstoken.sign.common.exceptions.NotSettledSessionException
import io.crosstoken.sign.common.exceptions.SESSION_IS_NOT_ACKNOWLEDGED_MESSAGE
import io.crosstoken.sign.common.model.vo.clientsync.session.SignRpc
import io.crosstoken.sign.common.model.vo.clientsync.session.params.SignParams
import io.crosstoken.sign.storage.sequence.SessionStorageRepository
import kotlinx.coroutines.supervisorScope

internal class ExtendSessionUseCase(
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val sessionStorageRepository: SessionStorageRepository,
    private val logger: Logger,
) : ExtendSessionUseCaseInterface {

    override suspend fun extend(topic: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        if (!sessionStorageRepository.isSessionValid(Topic(topic))) {
            return@supervisorScope onFailure(CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic"))
        }

        val session = sessionStorageRepository.getSessionWithoutMetadataByTopic(Topic(topic))
        if (!session.isAcknowledged) {
            logger.error("Sending session extend error: not acknowledged session on topic: $topic")
            return@supervisorScope onFailure(NotSettledSessionException("$SESSION_IS_NOT_ACKNOWLEDGED_MESSAGE$topic"))
        }

        val newExpiration = session.expiry.seconds + weekInSeconds
        sessionStorageRepository.extendSession(Topic(topic), newExpiration)
        val sessionExtend = SignRpc.SessionExtend(params = SignParams.ExtendParams(newExpiration))
        val irnParams = IrnParams(Tags.SESSION_EXTEND, Ttl(dayInSeconds), correlationId = sessionExtend.id)

        logger.log("Sending session extend on topic: $topic")
        jsonRpcInteractor.publishJsonRpcRequest(
            Topic(topic), irnParams, sessionExtend,
            onSuccess = {
                logger.log("Session extend sent successfully on topic: $topic")
                onSuccess()
            },
            onFailure = { error ->
                logger.error("Sending session extend error: $error on topic: $topic")
                onFailure(error)
            })
    }
}

internal interface ExtendSessionUseCaseInterface {
    suspend fun extend(topic: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}