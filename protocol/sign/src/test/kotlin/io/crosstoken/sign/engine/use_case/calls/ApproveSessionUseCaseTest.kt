package io.crosstoken.sign.engine.use_case.calls

import io.crosstoken.android.internal.common.crypto.kmr.KeyManagementRepository
import io.crosstoken.android.internal.common.model.AppMetaData
import io.crosstoken.android.internal.common.model.Expiry
import io.crosstoken.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import io.crosstoken.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import io.crosstoken.android.internal.common.storage.verify.VerifyContextStorageRepository
import io.crosstoken.android.pulse.domain.InsertTelemetryEventUseCase
import io.crosstoken.foundation.common.model.PublicKey
import io.crosstoken.foundation.common.model.Topic
import io.crosstoken.foundation.util.Logger
import io.crosstoken.sign.common.exceptions.InvalidNamespaceException
import io.crosstoken.sign.common.exceptions.SessionProposalExpiredException
import io.crosstoken.sign.common.model.vo.proposal.ProposalVO
import io.crosstoken.sign.engine.model.EngineDO
import io.crosstoken.sign.storage.proposal.ProposalStorageRepository
import io.crosstoken.sign.storage.sequence.SessionStorageRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.invoke
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ApproveSessionUseCaseTest {
    private lateinit var approveSessionUseCase: ApproveSessionUseCase
    private val jsonRpcInteractor = mockk<RelayJsonRpcInteractorInterface>(relaxed = true)
    private val crypto = mockk<KeyManagementRepository>(relaxed = true)
    private val sessionStorageRepository = mockk<SessionStorageRepository>(relaxed = true)
    private val proposalStorageRepository = mockk<ProposalStorageRepository>(relaxed = true)
    private val metadataStorageRepository = mockk<MetadataStorageRepositoryInterface>(relaxed = true)
    private val verifyContextStorageRepository = mockk<VerifyContextStorageRepository>(relaxed = true)
    private val insertEventUseCase = mockk<InsertTelemetryEventUseCase>(relaxed = true)
    private val logger = mockk<Logger>(relaxed = true)
    private val selfAppMetaData = mockk<AppMetaData>(relaxed = true)

    @Before
    fun setUp() {
        approveSessionUseCase = ApproveSessionUseCase(
            jsonRpcInteractor,
            crypto,
            sessionStorageRepository,
            proposalStorageRepository,
            metadataStorageRepository,
            verifyContextStorageRepository,
            selfAppMetaData,
            insertEventUseCase,
            logger
        )
    }

    @Test
    fun `approve should call onSuccess when session is approved successfully`() = runTest {
        val proposerPublicKey = "proposerPublicKey"
        val selfPublicKey = "selfPublicKey"
        val sessionNamespaces = mapOf(
            "eip155" to EngineDO.Namespace.Session(
                chains = listOf("eip155:1", "eip155:42161"),
                methods = listOf("eth_sendTransaction", "eth_signTransaction", "personal_sign", "eth_signTypedData"),
                events = listOf("chainChanged", "accountsChanged"),
                accounts = listOf("eip155:1:0x1234556")
            )
        )
        val proposal = mockk<ProposalVO>(relaxed = true) {
            every { expiry } returns null
        }
        val sessionTopic = Topic("sessionTopic")

        coEvery { proposalStorageRepository.getProposalByKey(any()) } returns proposal
        coEvery { crypto.generateAndStoreX25519KeyPair() } returns PublicKey(selfPublicKey)
        coEvery { crypto.generateTopicFromKeyAgreement(any(), any()) } returns sessionTopic
        coEvery { crypto.getSelfPublicFromKeyAgreement(any()) } returns PublicKey(selfPublicKey)
        coEvery { jsonRpcInteractor.subscribe(any(), any(), any()) } just Runs
        coEvery { jsonRpcInteractor.respondWithParams(any(), any(), any(), any(), any(), any()) } just Runs
        coEvery { jsonRpcInteractor.publishJsonRpcResponse(any(), any(), any(), captureLambda(), any(), any(), any()) } answers {
            lambda<() -> Unit>().invoke()
        }
        coEvery { sessionStorageRepository.insertSession(any(), any()) } just Runs
        coEvery { metadataStorageRepository.insertOrAbortMetadata(any(), any(), any()) } just Runs
        coEvery { metadataStorageRepository.insertOrAbortMetadata(any(), any(), any()) } just Runs
        every { logger.log(any<String>()) } just Runs
        val onSuccess = mockk<() -> Unit>(relaxed = true)

        approveSessionUseCase.approve(
            proposerPublicKey,
            sessionNamespaces,
            onSuccess = onSuccess,
            onFailure = {}
        )

        coVerify {
            jsonRpcInteractor.publishJsonRpcRequest(
                eq(sessionTopic),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        }
    }

    @Test
    fun `approve should call onFailure when proposal is expired`() = runTest {
        val proposerPublicKey = "proposerPublicKey"
        val sessionNamespaces = mapOf(
            "eip155" to EngineDO.Namespace.Session(
                chains = listOf("eip155:1", "eip155:42161"),
                methods = listOf("eth_sendTransaction", "eth_signTransaction", "personal_sign", "eth_signTypedData"),
                events = listOf("chainChanged", "accountsChanged"),
                accounts = listOf("eip155:1:0x1234556")
            )
        )
        val proposal = mockk<ProposalVO>(relaxed = true) {
            every { expiry } returns Expiry(0L)
        }

        coEvery { proposalStorageRepository.getProposalByKey(proposerPublicKey) } returns proposal
        val onFailure = mockk<(Throwable) -> Unit>(relaxed = true)

        approveSessionUseCase.approve(
            proposerPublicKey,
            sessionNamespaces,
            onSuccess = {},
            onFailure = onFailure
        )

        coVerify { onFailure.invoke(any<SessionProposalExpiredException>()) }
    }

    @Test
    fun `approve should call onFailure when namespace validation fails`() = runTest {
        val proposerPublicKey = "proposerPublicKey"
        val sessionNamespaces = mapOf(
            "eip155" to EngineDO.Namespace.Session(
                chains = listOf("eip155:1:213442343243223", "eip155:42161"),
                methods = listOf("eth_sendTransaction", "eth_signTransaction", "personal_sign", "eth_signTypedData"),
                events = listOf("chainChanged", "accountsChanged"),
                accounts = listOf("eip155:1:0x1234556")
            )
        )
        val proposal = mockk<ProposalVO>(relaxed = true) {
            every { expiry } returns null
        }

        coEvery { proposalStorageRepository.getProposalByKey(proposerPublicKey) } returns proposal
        val onFailure = mockk<(Throwable) -> Unit>(relaxed = true)

        approveSessionUseCase.approve(
            proposerPublicKey,
            sessionNamespaces,
            onSuccess = {},
            onFailure = onFailure
        )

        coVerify { onFailure.invoke(any<InvalidNamespaceException>()) }
    }

    @Test
    fun `approve should call onFailure when session settle fails`() = runTest {
        val proposerPublicKey = "proposerPublicKey"
        val sessionNamespaces = mapOf<String, EngineDO.Namespace.Session>()
        val proposal = mockk<ProposalVO>(relaxed = true)
        val sessionTopic = mockk<Topic>()

        coEvery { proposalStorageRepository.getProposalByKey(proposerPublicKey) } returns proposal
        coEvery { crypto.generateAndStoreX25519KeyPair() } returns mockk()
        coEvery { crypto.generateTopicFromKeyAgreement(any(), any()) } returns sessionTopic

        coEvery { jsonRpcInteractor.publishJsonRpcRequest(any(), any(), any(), any(), any()) } answers {
            lastArg<(Throwable) -> Unit>().invoke(Exception("Session settle failure"))
        }

        val onFailure = mockk<(Throwable) -> Unit>(relaxed = true)

        approveSessionUseCase.approve(
            proposerPublicKey,
            sessionNamespaces,
            onSuccess = {},
            onFailure = onFailure
        )

        coVerify { onFailure.invoke(any<Exception>()) }
    }
}