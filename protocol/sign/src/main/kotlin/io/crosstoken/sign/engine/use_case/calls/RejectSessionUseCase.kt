package io.crosstoken.sign.engine.use_case.calls

import io.crosstoken.android.Core
import io.crosstoken.android.internal.common.model.IrnParams
import io.crosstoken.android.internal.common.model.Tags
import io.crosstoken.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import io.crosstoken.android.internal.common.scope
import io.crosstoken.android.internal.common.storage.verify.VerifyContextStorageRepository
import io.crosstoken.android.internal.utils.CoreValidator.isExpired
import io.crosstoken.android.internal.utils.fiveMinutesInSeconds
import io.crosstoken.android.pairing.handler.PairingControllerInterface
import io.crosstoken.foundation.common.model.Ttl
import io.crosstoken.foundation.util.Logger
import io.crosstoken.sign.common.exceptions.PeerError
import io.crosstoken.sign.common.exceptions.SessionProposalExpiredException
import io.crosstoken.sign.engine.model.mapper.toSessionProposeRequest
import io.crosstoken.sign.storage.proposal.ProposalStorageRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class RejectSessionUseCase(
    private val verifyContextStorageRepository: VerifyContextStorageRepository,
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val proposalStorageRepository: ProposalStorageRepository,
    private val logger: Logger
) : RejectSessionUseCaseInterface {

    override suspend fun reject(proposerPublicKey: String, reason: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val proposal = proposalStorageRepository.getProposalByKey(proposerPublicKey)
        proposal.expiry?.let {
            if (it.isExpired()) {
                logger.error("Proposal expired on reject, topic: ${proposal.pairingTopic.value}, id: ${proposal.requestId}")
                throw SessionProposalExpiredException("Session proposal expired")
            }
        }

        logger.log("Sending session rejection, topic: ${proposal.pairingTopic.value}")
        jsonRpcInteractor.respondWithError(
            proposal.toSessionProposeRequest(),
            PeerError.EIP1193.UserRejectedRequest(reason),
            IrnParams(Tags.SESSION_PROPOSE_RESPONSE_REJECT, Ttl(fiveMinutesInSeconds), correlationId = proposal.requestId),
            onSuccess = {
                logger.log("Session rejection sent successfully, topic: ${proposal.pairingTopic.value}")
                scope.launch {
                    proposalStorageRepository.deleteProposal(proposerPublicKey)
                    verifyContextStorageRepository.delete(proposal.requestId)
                    jsonRpcInteractor.unsubscribe(proposal.pairingTopic)
                }
                onSuccess()
            },
            onFailure = { error ->
                logger.error("Session rejection sent failure, topic: ${proposal.pairingTopic.value}. Error: $error")
                onFailure(error)
            })
    }
}

internal interface RejectSessionUseCaseInterface {
    suspend fun reject(proposerPublicKey: String, reason: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit = {})
}