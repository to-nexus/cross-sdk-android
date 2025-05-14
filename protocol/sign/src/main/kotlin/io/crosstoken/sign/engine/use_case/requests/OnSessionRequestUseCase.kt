package io.crosstoken.sign.engine.use_case.requests

import io.crosstoken.android.internal.common.exception.Invalid
import io.crosstoken.android.internal.common.exception.Uncategorized
import io.crosstoken.android.internal.common.model.AppMetaData
import io.crosstoken.android.internal.common.model.AppMetaDataType
import io.crosstoken.android.internal.common.model.Expiry
import io.crosstoken.android.internal.common.model.IrnParams
import io.crosstoken.android.internal.common.model.Namespace
import io.crosstoken.android.internal.common.model.SDKError
import io.crosstoken.android.internal.common.model.Tags
import io.crosstoken.android.internal.common.model.TransportType
import io.crosstoken.android.internal.common.model.WCRequest
import io.crosstoken.android.internal.common.model.type.EngineEvent
import io.crosstoken.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import io.crosstoken.android.internal.common.scope
import io.crosstoken.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import io.crosstoken.android.internal.utils.CoreValidator.isExpired
import io.crosstoken.android.internal.utils.fiveMinutesInSeconds
import io.crosstoken.android.pulse.domain.InsertEventUseCase
import io.crosstoken.android.pulse.model.Direction
import io.crosstoken.android.pulse.model.EventType
import io.crosstoken.android.pulse.model.properties.Properties
import io.crosstoken.android.pulse.model.properties.Props
import io.crosstoken.android.verify.domain.ResolveAttestationIdUseCase
import io.crosstoken.android.verify.model.VerifyContext
import io.crosstoken.foundation.common.model.Ttl
import io.crosstoken.foundation.util.Logger
import io.crosstoken.sign.common.model.type.Sequences
import io.crosstoken.sign.common.model.vo.clientsync.session.params.SignParams
import io.crosstoken.sign.common.validator.SignValidator
import io.crosstoken.sign.engine.model.EngineDO
import io.crosstoken.sign.engine.model.mapper.toEngineDO
import io.crosstoken.sign.engine.model.mapper.toPeerError
import io.crosstoken.sign.engine.sessionRequestEventsQueue
import io.crosstoken.sign.storage.sequence.SessionStorageRepository
import io.crosstoken.utils.Empty
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class OnSessionRequestUseCase(
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val sessionStorageRepository: SessionStorageRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val resolveAttestationIdUseCase: ResolveAttestationIdUseCase,
    private val insertEventUseCase: InsertEventUseCase,
    private val clientId: String,
    private val logger: Logger
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(request: WCRequest, params: SignParams.SessionRequestParams) = supervisorScope {
        val irnParams = IrnParams(Tags.SESSION_REQUEST_RESPONSE, Ttl(fiveMinutesInSeconds), correlationId = request.id)
        logger.log("Session request received on topic: ${request.topic}")

        try {
            params.request.expiryTimestamp?.let {
                if (Expiry(it).isExpired()) {
                    logger.error("Session request received failure on topic: ${request.topic} - request expired")
                    jsonRpcInteractor.respondWithError(request, Invalid.RequestExpired, irnParams)
                    return@supervisorScope
                }
            }

            SignValidator.validateSessionRequest(params.toEngineDO(request.topic)) { error ->
                logger.error("Session request received failure on topic: ${request.topic} - invalid request")
                jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                return@supervisorScope
            }

            if (!sessionStorageRepository.isSessionValid(request.topic)) {
                logger.error("Session request received failure on topic: ${request.topic} - invalid session")
                jsonRpcInteractor.respondWithError(
                    request,
                    Uncategorized.NoMatchingTopic(Sequences.SESSION.name, request.topic.value),
                    irnParams
                )
                return@supervisorScope
            }
            val (sessionNamespaces: Map<String, Namespace.Session>, sessionPeerAppMetaData: AppMetaData?) =
                sessionStorageRepository.getSessionWithoutMetadataByTopic(request.topic)
                    .run {
                        val peerAppMetaData = metadataStorageRepository.getByTopicAndType(this.topic, AppMetaDataType.PEER)
                        this.sessionNamespaces to peerAppMetaData
                    }

            val method = params.request.method
            SignValidator.validateChainIdWithMethodAuthorisation(params.chainId, method, sessionNamespaces) { error ->
                jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                return@supervisorScope
            }

            if (request.transportType == TransportType.LINK_MODE) {
                insertEventUseCase(
                    Props(
                        EventType.SUCCESS,
                        Tags.SESSION_REQUEST_LINK_MODE.id.toString(),
                        Properties(correlationId = request.id, clientId = clientId, direction = Direction.RECEIVED.state)
                    )
                )
            }

            val url = sessionPeerAppMetaData?.url ?: String.Empty
            logger.log("Resolving session request attestation: ${System.currentTimeMillis()}")
            resolveAttestationIdUseCase(request, url, linkMode = request.transportType == TransportType.LINK_MODE, appLink = sessionPeerAppMetaData?.redirect?.universal) { verifyContext ->
                logger.log("Session request attestation resolved: ${System.currentTimeMillis()}")
                emitSessionRequest(params, request, sessionPeerAppMetaData, verifyContext)
            }
        } catch (e: Exception) {
            logger.error("Session request received failure on topic: ${request.topic} - ${e.message}")
            jsonRpcInteractor.respondWithError(
                request,
                Uncategorized.GenericError("Cannot handle a session request: ${e.message}, topic: ${request.topic}"),
                irnParams
            )
            _events.emit(SDKError(e))
            return@supervisorScope
        }
    }

    private fun emitSessionRequest(
        params: SignParams.SessionRequestParams,
        request: WCRequest,
        sessionPeerAppMetaData: AppMetaData?,
        verifyContext: VerifyContext
    ) {
        val sessionRequestEvent = EngineDO.SessionRequestEvent(params.toEngineDO(request, sessionPeerAppMetaData), verifyContext.toEngineDO())
        val event = if (sessionRequestEventsQueue.isEmpty()) {
            sessionRequestEvent
        } else {
            sessionRequestEventsQueue.find { event -> if (event.request.expiry != null) !event.request.expiry.isExpired() else true } ?: sessionRequestEvent
        }

        sessionRequestEventsQueue.add(sessionRequestEvent)
        logger.log("Session request received on topic: ${request.topic} - emitting")
        scope.launch { _events.emit(event) }
    }
}