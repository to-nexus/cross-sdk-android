package io.crosstoken.sign.engine.use_case.calls

import io.crosstoken.android.internal.common.JsonRpcResponse
import io.crosstoken.android.internal.common.exception.CannotFindSequenceForTopic
import io.crosstoken.android.internal.common.exception.RequestExpiredException
import io.crosstoken.android.internal.common.json_rpc.domain.link_mode.LinkModeJsonRpcInteractorInterface
import io.crosstoken.android.internal.common.model.AppMetaData
import io.crosstoken.android.internal.common.model.Expiry
import io.crosstoken.android.internal.common.model.TransportType
import io.crosstoken.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import io.crosstoken.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import io.crosstoken.android.internal.common.storage.verify.VerifyContextStorageRepository
import io.crosstoken.android.pulse.domain.InsertEventUseCase
import io.crosstoken.foundation.common.model.PublicKey
import io.crosstoken.foundation.common.model.Topic
import io.crosstoken.foundation.util.Logger
import io.crosstoken.sign.common.model.Request
import io.crosstoken.sign.common.model.vo.clientsync.session.params.SignParams
import io.crosstoken.sign.common.model.vo.clientsync.session.payload.SessionRequestVO
import io.crosstoken.sign.common.model.vo.sequence.SessionVO
import io.crosstoken.sign.engine.model.tvf.TVF
import io.crosstoken.sign.json_rpc.domain.GetPendingJsonRpcHistoryEntryByIdUseCase
import io.crosstoken.sign.storage.sequence.SessionStorageRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.invoke
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class RespondSessionRequestUseCaseTest {
    private val jsonRpcInteractor = mockk<RelayJsonRpcInteractorInterface>()
    private val sessionStorageRepository = mockk<SessionStorageRepository>()
    private val getPendingJsonRpcHistoryEntryByIdUseCase = mockk<GetPendingJsonRpcHistoryEntryByIdUseCase>()
    private val logger = mockk<Logger>()
    private val verifyContextStorageRepository = mockk<VerifyContextStorageRepository>()
    private val metadataStorageRepository = mockk<MetadataStorageRepositoryInterface>()
    private val linkModeJsonRpcInteractor: LinkModeJsonRpcInteractorInterface = mockk()
    private val tvf: TVF = mockk()
    private val insertEventUseCase = mockk<InsertEventUseCase>()
    private val respondSessionRequestUseCase = RespondSessionRequestUseCase(
        jsonRpcInteractor,
        sessionStorageRepository,
        getPendingJsonRpcHistoryEntryByIdUseCase,
        linkModeJsonRpcInteractor,
        logger,
        verifyContextStorageRepository,
        metadataStorageRepository,
        insertEventUseCase,
        "clientId",
        tvf
    )

    @Before
    fun setUp() {
        every { logger.error(any() as String) } answers { }
        every { logger.error(any() as Exception) } answers { }
    }

    @Test
    fun `onFailure is called when sessionStorageRepository isSessionValid is false`() = runTest {
        every { sessionStorageRepository.isSessionValid(any()) } returns false

        respondSessionRequestUseCase.respondSessionRequest(
            topic = "topic",
            jsonRpcResponse = JsonRpcResponse.JsonRpcResult(1, "2.0", "result"),
            onSuccess = {
                Assert.fail("onSuccess should not be called since should have validation failed")
            },
            onFailure = { error ->
                Assert.assertSame(CannotFindSequenceForTopic::class, error::class)
            }
        )
    }

    @Test
    fun `should call onFailure when pending request is null`() = runTest {
        val session = SessionVO(
            topic = Topic("topic"),
            expiry = Expiry(0),
            relayProtocol = "relayProtocol",
            relayData = "relayData",
            controllerKey = PublicKey(""),
            selfPublicKey = PublicKey(""),
            sessionNamespaces = emptyMap(),
            requiredNamespaces = emptyMap(),
            optionalNamespaces = emptyMap(),
            isAcknowledged = false,
            pairingTopic = "pairingTopic",
            transportType = null
        )
        // Arrange
        val topic = "testTopic"
        val jsonRpcResponse: JsonRpcResponse = mockk(relaxed = true)
        every { sessionStorageRepository.isSessionValid(Topic(topic)) } returns true
        coEvery { sessionStorageRepository.getSessionWithoutMetadataByTopic(any()) } returns session
        every { getPendingJsonRpcHistoryEntryByIdUseCase(jsonRpcResponse.id) } returns null
        coEvery { metadataStorageRepository.getByTopicAndType(any(), any()) } returns AppMetaData("", "", listOf(), "")

        val onFailure = mockk<(Throwable) -> Unit>(relaxed = true)

        // Act
        respondSessionRequestUseCase.respondSessionRequest(topic, jsonRpcResponse, onSuccess = { }, onFailure = onFailure)

        // Assert
        coVerify { onFailure(ofType(RequestExpiredException::class)) }
    }

    @Test
    fun `should publish response and call onSuccess`() = runTest {
        // Arrange
        val pendingRequest = Request<SignParams.SessionRequestParams>(
            id = 1,
            topic = Topic("topic"),
            chainId = "eip155:1",
            method = "method",
            params = SignParams.SessionRequestParams(
                SessionRequestVO(
                    method = "personal_sign",
                    params = "params"
                ), "eip155:1"
            ),
            transportType = TransportType.RELAY

        )
        val session = SessionVO(
            topic = Topic("topic"),
            expiry = Expiry(0),
            relayProtocol = "relayProtocol",
            relayData = "relayData",
            controllerKey = PublicKey(""),
            selfPublicKey = PublicKey(""),
            sessionNamespaces = emptyMap(),
            requiredNamespaces = emptyMap(),
            optionalNamespaces = emptyMap(),
            isAcknowledged = false,
            pairingTopic = "pairingTopic",
            transportType = null
        )
        // Arrange
        val topic = "testTopic"
        val jsonRpcResponse: JsonRpcResponse = JsonRpcResponse.JsonRpcResult(id = 1, result = "result")
        every { sessionStorageRepository.isSessionValid(Topic(topic)) } returns true
        coEvery { sessionStorageRepository.getSessionWithoutMetadataByTopic(any()) } returns session
        every { getPendingJsonRpcHistoryEntryByIdUseCase(jsonRpcResponse.id) } returns null
        coEvery { metadataStorageRepository.getByTopicAndType(any(), any()) } returns AppMetaData("", "", listOf(), "")
        every { sessionStorageRepository.isSessionValid(Topic(topic)) } returns true
        every { getPendingJsonRpcHistoryEntryByIdUseCase(jsonRpcResponse.id) } returns pendingRequest
        every { tvf.collect(any(), any(), any()) } returns Triple(listOf("rpcMethod"), listOf("contractAddress"), "chainId")
        every { tvf.collectTxHashes(any(), any()) } returns listOf("hash")
        every { logger.log(any<String>()) } just Runs
        every { jsonRpcInteractor.publishJsonRpcResponse(any(), any(), any(), captureLambda(), any(), any(), any()) } answers {
            lambda<() -> Unit>().invoke()
        }

        val onSuccess = mockk<() -> Unit>(relaxed = true)

        // Act
        respondSessionRequestUseCase.respondSessionRequest(topic, jsonRpcResponse, onSuccess = onSuccess, onFailure = {})

        // Assert
        coVerify { jsonRpcInteractor.publishJsonRpcResponse(any(), any(), any(), any(), any(), any()) }
        coVerify { onSuccess.invoke() }
    }

    @Test
    fun `should call onFailure when publishJsonRpcResponse fails`() = runTest {
        // Arrange
        val pendingRequest = Request<SignParams.SessionRequestParams>(
            id = 1,
            topic = Topic("topic"),
            chainId = "eip155:1",
            method = "method",
            params = SignParams.SessionRequestParams(
                SessionRequestVO(
                    method = "personal_sign",
                    params = "params"
                ), "eip155:1"
            ),
            transportType = TransportType.RELAY

        )
        val session = SessionVO(
            topic = Topic("topic"),
            expiry = Expiry(0),
            relayProtocol = "relayProtocol",
            relayData = "relayData",
            controllerKey = PublicKey(""),
            selfPublicKey = PublicKey(""),
            sessionNamespaces = emptyMap(),
            requiredNamespaces = emptyMap(),
            optionalNamespaces = emptyMap(),
            isAcknowledged = false,
            pairingTopic = "pairingTopic",
            transportType = null
        )
        // Arrange
        val topic = "testTopic"
        val jsonRpcResponse: JsonRpcResponse = JsonRpcResponse.JsonRpcResult(id = 1, result = "result")
        every { sessionStorageRepository.isSessionValid(Topic(topic)) } returns true
        coEvery { sessionStorageRepository.getSessionWithoutMetadataByTopic(any()) } returns session
        every { getPendingJsonRpcHistoryEntryByIdUseCase(jsonRpcResponse.id) } returns null
        coEvery { metadataStorageRepository.getByTopicAndType(any(), any()) } returns AppMetaData("", "", listOf(), "")
        every { sessionStorageRepository.isSessionValid(Topic(topic)) } returns true
        every { getPendingJsonRpcHistoryEntryByIdUseCase(jsonRpcResponse.id) } returns pendingRequest
        every { tvf.collect(any(), any(), any()) } returns Triple(listOf("rpcMethod"), listOf("contractAddress"), "chainId")
        every { tvf.collectTxHashes(any(), any()) } returns listOf("hash")
        every { logger.log(any<String>()) } just Runs
        every { jsonRpcInteractor.publishJsonRpcResponse(any(), any(), any(), any(), captureLambda(), any(), any()) } answers {
            lambda<(Throwable) -> Unit>().invoke(Throwable("Publish fails"))
        }

        val onFailure: (Throwable) -> Unit = mockk(relaxed = true)

        // Act
        respondSessionRequestUseCase.respondSessionRequest(topic, jsonRpcResponse, onSuccess = {}, onFailure = onFailure)

        // Assert
        coVerify { jsonRpcInteractor.publishJsonRpcResponse(any(), any(), any(), any(), any(), any(), any()) }
        verify { onFailure.invoke(ofType(Throwable::class)) }
    }
}