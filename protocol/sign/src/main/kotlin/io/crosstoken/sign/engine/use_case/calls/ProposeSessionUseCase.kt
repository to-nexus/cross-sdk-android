package io.crosstoken.sign.engine.use_case.calls

import io.crosstoken.android.internal.common.crypto.kmr.KeyManagementRepository
import io.crosstoken.android.internal.common.model.AppMetaData
import io.crosstoken.android.internal.common.model.Expiry
import io.crosstoken.android.internal.common.model.IrnParams
import io.crosstoken.android.internal.common.model.Pairing
import io.crosstoken.android.internal.common.model.RelayProtocolOptions
import io.crosstoken.android.internal.common.model.Tags
import io.crosstoken.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import io.crosstoken.android.internal.utils.PROPOSAL_EXPIRY
import io.crosstoken.android.internal.utils.fiveMinutesInSeconds
import io.crosstoken.foundation.common.model.PublicKey
import io.crosstoken.foundation.common.model.Ttl
import io.crosstoken.foundation.util.Logger
import io.crosstoken.sign.common.exceptions.InvalidNamespaceException
import io.crosstoken.sign.common.exceptions.InvalidPropertiesException
import io.crosstoken.sign.common.model.vo.clientsync.session.SignRpc
import io.crosstoken.sign.common.model.vo.clientsync.session.params.SignParams
import io.crosstoken.sign.common.validator.SignValidator
import io.crosstoken.sign.engine.model.EngineDO
import io.crosstoken.sign.engine.model.mapper.toNamespacesVOOptional
import io.crosstoken.sign.engine.model.mapper.toNamespacesVORequired
import io.crosstoken.sign.engine.model.mapper.toSessionProposeParams
import io.crosstoken.sign.engine.model.mapper.toVO
import io.crosstoken.sign.storage.proposal.ProposalStorageRepository
import kotlinx.coroutines.supervisorScope

internal class ProposeSessionUseCase(
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val crypto: KeyManagementRepository,
    private val proposalStorageRepository: ProposalStorageRepository,
    private val selfAppMetaData: AppMetaData,
    private val logger: Logger
) : ProposeSessionUseCaseInterface {

    override suspend fun proposeSession(
        requiredNamespaces: Map<String, EngineDO.Namespace.Proposal>?,
        optionalNamespaces: Map<String, EngineDO.Namespace.Proposal>?,
        properties: Map<String, String>?,
        scopedProperties: Map<String, String>?,
        pairing: Pairing,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ) = supervisorScope {
        val relay = RelayProtocolOptions(pairing.relayProtocol, pairing.relayData)

        runCatching { validate(requiredNamespaces, optionalNamespaces, properties) }.fold(
            onSuccess = {
                val expiry = Expiry(PROPOSAL_EXPIRY)
                val selfPublicKey: PublicKey = crypto.generateAndStoreX25519KeyPair()
                val sessionProposal: SignParams.SessionProposeParams =
                    toSessionProposeParams(
                        listOf(relay),
                        requiredNamespaces ?: emptyMap(),
                        optionalNamespaces ?: emptyMap(),
                        properties, scopedProperties, selfPublicKey, selfAppMetaData, expiry
                    )
                val request = SignRpc.SessionPropose(params = sessionProposal)
                proposalStorageRepository.insertProposal(sessionProposal.toVO(pairing.topic, request.id))
                val irnParams = IrnParams(Tags.SESSION_PROPOSE, Ttl(fiveMinutesInSeconds), correlationId = request.id, prompt = true)
                jsonRpcInteractor.subscribe(pairing.topic) { error -> onFailure(error) }

                logger.log("Sending proposal on topic: ${pairing.topic.value}")
                jsonRpcInteractor.publishJsonRpcRequest(pairing.topic, irnParams, request,
                    onSuccess = {
                        logger.log("Session proposal sent successfully, topic: ${pairing.topic}")
                        onSuccess()
                    },
                    onFailure = { error ->
                        logger.error("Failed to send a session proposal: $error")
                        onFailure(error)
                    }
                )
            },
            onFailure = { error ->
                logger.error("Failed to validate session proposal: $error")
                onFailure(error)
            }
        )
    }

    private fun validate(
        requiredNamespaces: Map<String, EngineDO.Namespace.Proposal>?,
        optionalNamespaces: Map<String, EngineDO.Namespace.Proposal>?,
        properties: Map<String, String>?
    ) {
        requiredNamespaces?.let { namespaces ->
            SignValidator.validateProposalNamespaces(namespaces.toNamespacesVORequired()) { error ->
                logger.error("Failed to send a session proposal - required namespaces error: $error")
                throw InvalidNamespaceException(error.message)
            }
        }

        optionalNamespaces?.let { namespaces ->
            SignValidator.validateProposalNamespaces(namespaces.toNamespacesVOOptional()) { error ->
                logger.error("Failed to send a session proposal - optional namespaces error: $error")
                throw InvalidNamespaceException(error.message)
            }
        }

        properties?.let {
            SignValidator.validateProperties(properties) { error ->
                logger.error("Failed to send a session proposal - session properties error: $error")
                throw InvalidPropertiesException(error.message)
            }
        }
    }
}

internal interface ProposeSessionUseCaseInterface {
    suspend fun proposeSession(
        requiredNamespaces: Map<String, EngineDO.Namespace.Proposal>?,
        optionalNamespaces: Map<String, EngineDO.Namespace.Proposal>?,
        properties: Map<String, String>?,
        scopedProperties: Map<String, String>?,
        pairing: Pairing,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    )
}