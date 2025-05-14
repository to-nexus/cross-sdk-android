package io.crosstoken.sign.engine.use_case.requests

import io.crosstoken.android.Core
import io.crosstoken.android.internal.common.crypto.kmr.KeyManagementRepository
import io.crosstoken.android.internal.common.model.AppMetaData
import io.crosstoken.android.internal.common.model.AppMetaDataType
import io.crosstoken.android.internal.common.model.IrnParams
import io.crosstoken.android.internal.common.model.SDKError
import io.crosstoken.android.internal.common.model.Tags
import io.crosstoken.android.internal.common.model.WCRequest
import io.crosstoken.android.internal.common.model.type.EngineEvent
import io.crosstoken.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import io.crosstoken.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import io.crosstoken.android.internal.utils.fiveMinutesInSeconds
import io.crosstoken.android.pairing.handler.PairingControllerInterface
import io.crosstoken.android.utils.toClient
import io.crosstoken.foundation.common.model.PublicKey
import io.crosstoken.foundation.common.model.Ttl
import io.crosstoken.foundation.util.Logger
import io.crosstoken.sign.common.exceptions.PeerError
import io.crosstoken.sign.common.model.vo.clientsync.session.params.SignParams
import io.crosstoken.sign.common.model.vo.sequence.SessionVO
import io.crosstoken.sign.common.validator.SignValidator
import io.crosstoken.sign.engine.model.mapper.toPeerError
import io.crosstoken.sign.engine.model.mapper.toSessionApproved
import io.crosstoken.sign.storage.proposal.ProposalStorageRepository
import io.crosstoken.sign.storage.sequence.SessionStorageRepository
import io.crosstoken.utils.Empty
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnSessionSettleUseCase(
    private val crypto: KeyManagementRepository,
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val proposalStorageRepository: ProposalStorageRepository,
    private val sessionStorageRepository: SessionStorageRepository,
    private val pairingController: PairingControllerInterface,
    private val selfAppMetaData: AppMetaData,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val logger: Logger
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(request: WCRequest, settleParams: SignParams.SessionSettleParams) = supervisorScope {
        logger.log("Session settle received on topic: ${request.topic}")
        val sessionTopic = request.topic
        val irnParams = IrnParams(Tags.SESSION_SETTLE_RESPONSE, Ttl(fiveMinutesInSeconds), correlationId = request.id)
        val selfPublicKey: PublicKey = try {
            crypto.getSelfPublicFromKeyAgreement(sessionTopic)
        } catch (e: Exception) {
            logger.error("Session settle received failure: ${request.topic}, error: $e")
            jsonRpcInteractor.respondWithError(request, PeerError.Failure.SessionSettlementFailed(e.message ?: String.Empty), irnParams)
            return@supervisorScope
        }

        val peerMetadata = settleParams.controller.metadata
        val proposal = try {
            proposalStorageRepository.getProposalByKey(selfPublicKey.keyAsHex).also { proposalStorageRepository.deleteProposal(selfPublicKey.keyAsHex) }
        } catch (e: Exception) {
            logger.error("Session settle received failure: ${request.topic}, error: $e")
            jsonRpcInteractor.respondWithError(request, PeerError.Failure.SessionSettlementFailed(e.message ?: String.Empty), irnParams)
            return@supervisorScope
        }

        val (requiredNamespaces, optionalNamespaces, properties) = proposal.run { Triple(requiredNamespaces, optionalNamespaces, properties) }
        SignValidator.validateSessionNamespace(settleParams.namespaces, requiredNamespaces) { error ->
            logger.error("Session settle received failure - namespace validation: ${request.topic}, error: $error")
            jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
            return@supervisorScope
        }

        try {
            val session = SessionVO.createAcknowledgedSession(
                sessionTopic,
                settleParams,
                selfPublicKey,
                selfAppMetaData,
                requiredNamespaces,
                optionalNamespaces,
                proposal.pairingTopic.value
            )

            sessionStorageRepository.insertSession(session, request.id)
            pairingController.updateMetadata(Core.Params.UpdateMetadata(proposal.pairingTopic.value, peerMetadata.toClient(), AppMetaDataType.PEER))
            metadataStorageRepository.insertOrAbortMetadata(sessionTopic, peerMetadata, AppMetaDataType.PEER)
            jsonRpcInteractor.respondWithSuccess(request, irnParams)
            logger.log("Session settle received on topic: ${request.topic} - emitting")
            _events.emit(session.toSessionApproved())
        } catch (e: Exception) {
            logger.error("Session settle received failure: ${request.topic}, error: $e")
            proposalStorageRepository.insertProposal(proposal)
            sessionStorageRepository.deleteSession(sessionTopic)
            jsonRpcInteractor.respondWithError(request, PeerError.Failure.SessionSettlementFailed(e.message ?: String.Empty), irnParams)
            _events.emit(SDKError(e))
            return@supervisorScope
        }
    }
}