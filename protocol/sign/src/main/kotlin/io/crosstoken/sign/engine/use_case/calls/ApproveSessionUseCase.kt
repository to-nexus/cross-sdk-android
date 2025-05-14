package io.crosstoken.sign.engine.use_case.calls

import io.crosstoken.android.internal.common.crypto.kmr.KeyManagementRepository
import io.crosstoken.android.internal.common.exception.NoInternetConnectionException
import io.crosstoken.android.internal.common.exception.NoRelayConnectionException
import io.crosstoken.android.internal.common.model.AppMetaData
import io.crosstoken.android.internal.common.model.AppMetaDataType
import io.crosstoken.android.internal.common.model.IrnParams
import io.crosstoken.android.internal.common.model.Tags
import io.crosstoken.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import io.crosstoken.android.internal.common.scope
import io.crosstoken.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import io.crosstoken.android.internal.common.storage.verify.VerifyContextStorageRepository
import io.crosstoken.android.internal.utils.ACTIVE_SESSION
import io.crosstoken.android.internal.utils.CoreValidator.isExpired
import io.crosstoken.android.internal.utils.fiveMinutesInSeconds
import io.crosstoken.android.pulse.domain.InsertTelemetryEventUseCase
import io.crosstoken.android.pulse.model.EventType
import io.crosstoken.android.pulse.model.Trace
import io.crosstoken.android.pulse.model.properties.Properties
import io.crosstoken.android.pulse.model.properties.Props
import io.crosstoken.foundation.common.model.PublicKey
import io.crosstoken.foundation.common.model.Topic
import io.crosstoken.foundation.common.model.Ttl
import io.crosstoken.foundation.util.Logger
import io.crosstoken.sign.common.exceptions.InvalidNamespaceException
import io.crosstoken.sign.common.exceptions.SessionProposalExpiredException
import io.crosstoken.sign.common.model.vo.clientsync.common.SessionParticipant
import io.crosstoken.sign.common.model.vo.clientsync.session.SignRpc
import io.crosstoken.sign.common.model.vo.proposal.ProposalVO
import io.crosstoken.sign.common.model.vo.sequence.SessionVO
import io.crosstoken.sign.common.validator.SignValidator
import io.crosstoken.sign.engine.model.EngineDO
import io.crosstoken.sign.engine.model.mapper.toMapOfNamespacesVOSession
import io.crosstoken.sign.engine.model.mapper.toSessionApproveParams
import io.crosstoken.sign.engine.model.mapper.toSessionProposeRequest
import io.crosstoken.sign.engine.model.mapper.toSessionSettleParams
import io.crosstoken.sign.storage.proposal.ProposalStorageRepository
import io.crosstoken.sign.storage.sequence.SessionStorageRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class ApproveSessionUseCase(
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val crypto: KeyManagementRepository,
    private val sessionStorageRepository: SessionStorageRepository,
    private val proposalStorageRepository: ProposalStorageRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val verifyContextStorageRepository: VerifyContextStorageRepository,
    private val selfAppMetaData: AppMetaData,
    private val insertEventUseCase: InsertTelemetryEventUseCase,
    private val logger: Logger
) : ApproveSessionUseCaseInterface {

    override suspend fun approve(
        proposerPublicKey: String,
        sessionNamespaces: Map<String, EngineDO.Namespace.Session>,
        sessionProperties: Map<String, String>?,
        scopedProperties: Map<String, String>?,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) = supervisorScope {
        val trace: MutableList<String> = mutableListOf()
        trace.add(Trace.Session.SESSION_APPROVE_STARTED).also { logger.log(Trace.Session.SESSION_APPROVE_STARTED) }
        fun sessionSettle(requestId: Long, proposal: ProposalVO, sessionTopic: Topic, pairingTopic: Topic) {
            val selfPublicKey = crypto.getSelfPublicFromKeyAgreement(sessionTopic)
            val selfParticipant = SessionParticipant(selfPublicKey.keyAsHex, selfAppMetaData)
            val sessionExpiry = ACTIVE_SESSION
            val unacknowledgedSession =
                SessionVO.createUnacknowledgedSession(sessionTopic, proposal, selfParticipant, sessionExpiry, sessionNamespaces, scopedProperties, sessionProperties, pairingTopic.value)
            try {
                sessionStorageRepository.insertSession(unacknowledgedSession, requestId)
                metadataStorageRepository.insertOrAbortMetadata(sessionTopic, selfAppMetaData, AppMetaDataType.SELF)
                metadataStorageRepository.insertOrAbortMetadata(sessionTopic, proposal.appMetaData, AppMetaDataType.PEER)
                trace.add(Trace.Session.STORE_SESSION)
                val params = proposal.toSessionSettleParams(selfParticipant, sessionExpiry, sessionNamespaces, sessionProperties, scopedProperties)
                val sessionSettle = SignRpc.SessionSettle(params = params)
                val irnParams = IrnParams(Tags.SESSION_SETTLE, Ttl(fiveMinutesInSeconds), correlationId = sessionSettle.id)
                trace.add(Trace.Session.PUBLISHING_SESSION_SETTLE).also { logger.log("Publishing session settle on topic: $sessionTopic") }
                jsonRpcInteractor.publishJsonRpcRequest(
                    topic = sessionTopic,
                    params = irnParams, sessionSettle,
                    onSuccess = {
                        onSuccess()
                        scope.launch {
                            supervisorScope {
                                proposalStorageRepository.deleteProposal(proposerPublicKey)
                                verifyContextStorageRepository.delete(proposal.requestId)
                                trace.add(Trace.Session.SESSION_SETTLE_PUBLISH_SUCCESS).also { logger.log("Session settle sent successfully on topic: $sessionTopic") }
                            }
                        }
                    },
                    onFailure = { error ->
                        scope.launch {
                            supervisorScope {
                                insertEventUseCase(Props(type = EventType.Error.SESSION_SETTLE_PUBLISH_FAILURE, properties = Properties(trace = trace, topic = pairingTopic.value)))
                            }
                        }.also { logger.error("Session settle failure on topic: $sessionTopic, error: $error") }
                        onFailure(error)
                    }
                )
            } catch (e: Exception) {
                if (e is NoRelayConnectionException)
                    scope.launch {
                        supervisorScope {
                            insertEventUseCase(Props(type = EventType.Error.NO_WSS_CONNECTION, properties = Properties(trace = trace, topic = pairingTopic.value)))
                        }
                    }
                if (e is NoInternetConnectionException)
                    scope.launch {
                        supervisorScope {
                            insertEventUseCase(Props(type = EventType.Error.NO_INTERNET_CONNECTION, properties = Properties(trace = trace, topic = pairingTopic.value)))
                        }
                    }
                sessionStorageRepository.deleteSession(sessionTopic)
                logger.error("Session settle failure, error: $e")
                // todo: missing metadata deletion. Also check other try catches
                onFailure(e)
            }
        }

        val proposal = proposalStorageRepository.getProposalByKey(proposerPublicKey)
        val request = proposal.toSessionProposeRequest()
        val pairingTopic = proposal.pairingTopic.value
        try {
            proposal.expiry?.let {
                if (it.isExpired()) {
                    insertEventUseCase(Props(type = EventType.Error.PROPOSAL_EXPIRED, properties = Properties(trace = trace, topic = pairingTopic)))
                        .also { logger.error("Proposal expired on approve, topic: $pairingTopic, id: ${proposal.requestId}") }
                    throw SessionProposalExpiredException("Session proposal expired")
                }
            }
            trace.add(Trace.Session.PROPOSAL_NOT_EXPIRED)
            SignValidator.validateSessionNamespace(sessionNamespaces.toMapOfNamespacesVOSession(), proposal.requiredNamespaces) { error ->
                insertEventUseCase(Props(type = EventType.Error.SESSION_APPROVE_NAMESPACE_VALIDATION_FAILURE, properties = Properties(trace = trace, topic = pairingTopic)))
                    .also { logger.log("Session approve failure - invalid namespaces, error: ${error.message}") }
                throw InvalidNamespaceException(error.message)
            }
            trace.add(Trace.Session.SESSION_NAMESPACE_VALIDATION_SUCCESS)
            val selfPublicKey: PublicKey = crypto.generateAndStoreX25519KeyPair()
            val sessionTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, PublicKey(proposerPublicKey))
            trace.add(Trace.Session.CREATE_SESSION_TOPIC)
            val approvalParams = proposal.toSessionApproveParams(selfPublicKey)
            val irnParams = IrnParams(Tags.SESSION_PROPOSE_RESPONSE_APPROVE, Ttl(fiveMinutesInSeconds), correlationId = proposal.requestId)
            trace.add(Trace.Session.SUBSCRIBING_SESSION_TOPIC).also { logger.log("Subscribing to session topic: $sessionTopic") }
            jsonRpcInteractor.subscribe(sessionTopic,
                onSuccess = {
                    trace.add(Trace.Session.SUBSCRIBE_SESSION_TOPIC_SUCCESS).also { logger.log("Successfully subscribed to session topic: $sessionTopic") }
                },
                onFailure = { error ->
                    scope.launch {
                        supervisorScope {
                            insertEventUseCase(Props(type = EventType.Error.SESSION_SUBSCRIPTION_FAILURE, properties = Properties(trace = trace, topic = pairingTopic)))
                        }
                    }.also { logger.error("Subscribe to session topic failure: $error") }
                    onFailure(error)
                })

            sessionSettle(request.id, proposal, sessionTopic, request.topic)

            trace.add(Trace.Session.PUBLISHING_SESSION_APPROVE).also { logger.log("Publishing session approve on topic: $sessionTopic") }
            jsonRpcInteractor.respondWithParams(request, approvalParams, irnParams,
                onSuccess = {
                    trace.add(Trace.Session.SESSION_APPROVE_PUBLISH_SUCCESS).also { logger.log("Session approve sent successfully, topic: $sessionTopic") }
                },
                onFailure = { error ->
                    scope.launch {
                        supervisorScope {
                            insertEventUseCase(Props(type = EventType.Error.SESSION_APPROVE_PUBLISH_FAILURE, properties = Properties(trace = trace, topic = pairingTopic)))
                        }
                    }.also { logger.error("Session approve failure, topic: $sessionTopic: $error") }
                    onFailure(error)
                })
        } catch (e: Exception) {
            if (e is NoRelayConnectionException) insertEventUseCase(Props(type = EventType.Error.NO_WSS_CONNECTION, properties = Properties(trace = trace, topic = pairingTopic)))
            if (e is NoInternetConnectionException) insertEventUseCase(Props(type = EventType.Error.NO_INTERNET_CONNECTION, properties = Properties(trace = trace, topic = pairingTopic)))
            onFailure(e)
        }
    }
}

internal interface ApproveSessionUseCaseInterface {
    suspend fun approve(
        proposerPublicKey: String,
        sessionNamespaces: Map<String, EngineDO.Namespace.Session>,
        sessionProperties: Map<String, String>? = null,
        scopedProperties: Map<String, String>? = null,
        onSuccess: () -> Unit = {},
        onFailure: (Throwable) -> Unit = {},
    )
}