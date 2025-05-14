package io.crosstoken.sign.engine.use_case.requests

import io.crosstoken.android.internal.common.exception.Uncategorized
import io.crosstoken.android.internal.common.model.IrnParams
import io.crosstoken.android.internal.common.model.SDKError
import io.crosstoken.android.internal.common.model.Tags
import io.crosstoken.android.internal.common.model.WCRequest
import io.crosstoken.android.internal.common.model.type.EngineEvent
import io.crosstoken.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import io.crosstoken.android.internal.utils.dayInSeconds
import io.crosstoken.foundation.common.model.Ttl
import io.crosstoken.foundation.util.Logger
import io.crosstoken.sign.common.exceptions.PeerError
import io.crosstoken.sign.common.model.type.Sequences
import io.crosstoken.sign.common.model.vo.clientsync.session.params.SignParams
import io.crosstoken.sign.common.model.vo.sequence.SessionVO
import io.crosstoken.sign.common.validator.SignValidator
import io.crosstoken.sign.engine.model.EngineDO
import io.crosstoken.sign.engine.model.mapper.toMapOfEngineNamespacesSession
import io.crosstoken.sign.storage.sequence.SessionStorageRepository
import io.crosstoken.utils.extractTimestamp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnSessionUpdateUseCase(
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val sessionStorageRepository: SessionStorageRepository,
    private val logger: Logger
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(request: WCRequest, params: SignParams.UpdateNamespacesParams) = supervisorScope {
        val irnParams = IrnParams(Tags.SESSION_UPDATE_RESPONSE, Ttl(dayInSeconds), correlationId = request.id)
        logger.log("Session update received on topic: ${request.topic}")
        try {
            if (!sessionStorageRepository.isSessionValid(request.topic)) {
                logger.error("Session update received failure on topic: ${request.topic} - invalid session")
                jsonRpcInteractor.respondWithError(request, Uncategorized.NoMatchingTopic(Sequences.SESSION.name, request.topic.value), irnParams)
                return@supervisorScope
            }

            val session: SessionVO = sessionStorageRepository.getSessionWithoutMetadataByTopic(request.topic)
            if (!session.isPeerController) {
                logger.error("Session update received failure on topic: ${request.topic} - unauthorized peer")
                jsonRpcInteractor.respondWithError(request, PeerError.Unauthorized.UpdateRequest(Sequences.SESSION.name), irnParams)
                return@supervisorScope
            }

            SignValidator.validateSessionNamespace(params.namespaces, session.requiredNamespaces) { error ->
                logger.error("Session update received failure on topic: ${request.topic} - namespaces validation: $error")
                jsonRpcInteractor.respondWithError(request, PeerError.Invalid.UpdateRequest(error.message), irnParams)
                return@supervisorScope
            }

            if (!sessionStorageRepository.isUpdatedNamespaceValid(session.topic.value, request.id.extractTimestamp())) {
                logger.error("Session update received failure on topic: ${request.topic} - Update Namespace Request ID too old")
                jsonRpcInteractor.respondWithError(request, PeerError.Invalid.UpdateRequest("Update Namespace Request ID too old"), irnParams)
                return@supervisorScope
            }

            sessionStorageRepository.deleteNamespaceAndInsertNewNamespace(session.topic.value, params.namespaces, request.id)
            jsonRpcInteractor.respondWithSuccess(request, irnParams)
            logger.log("Session update received on topic: ${request.topic} - emitting")
            _events.emit(EngineDO.SessionUpdateNamespaces(request.topic, params.namespaces.toMapOfEngineNamespacesSession()))
        } catch (e: Exception) {
            logger.error("Session update received failure on topic: ${request.topic} - $e")
            jsonRpcInteractor.respondWithError(
                request,
                PeerError.Invalid.UpdateRequest("Updating Namespace Failed. Review Namespace structure. Error: ${e.message}, topic: ${request.topic}"),
                irnParams
            )
            _events.emit(SDKError(e))
            return@supervisorScope
        }
    }
}