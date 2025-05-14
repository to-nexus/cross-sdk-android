package io.crosstoken.android.internal

import io.crosstoken.android.internal.common.crypto.sha256
import io.crosstoken.android.internal.common.model.TransportType
import io.crosstoken.android.internal.common.model.Validation
import io.crosstoken.android.internal.common.model.WCRequest
import io.crosstoken.android.internal.common.model.type.ClientParams
import io.crosstoken.android.internal.common.storage.verify.VerifyContextStorageRepository
import io.crosstoken.android.verify.client.VerifyInterface
import io.crosstoken.android.verify.domain.ResolveAttestationIdUseCase
import io.crosstoken.android.verify.domain.VerifyResult
import io.crosstoken.android.verify.model.VerifyContext
import io.crosstoken.foundation.common.model.Topic
import io.crosstoken.utils.Empty
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.invoke
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ResolveAttestationUseCaseTest {
    private var verifyInterface: VerifyInterface = mockk()
    private var repository: VerifyContextStorageRepository = mockk()
    private var resolveAttestationIdUseCase: ResolveAttestationIdUseCase
    private val verifyUrl = "https://verify.test.url"

    init {
        resolveAttestationIdUseCase = ResolveAttestationIdUseCase(verifyInterface, repository, verifyUrl)
    }

    data class TestParams(val test: String = "test") : ClientParams

    @Test
    fun `invoke should call resolveKLinkMode when linkMode is true and appLink is not empty`() {
        val metadataUrl = "https://metadata.url"
        val appLink = "https://metadata.url"
        val request = WCRequest(
            id = 1L,
            attestation = null,
            method = "method",
            params = TestParams(),
            topic = Topic("topic"),
            transportType = TransportType.LINK_MODE
        )

        coEvery { repository.insertOrAbort(any()) } just Runs

        val onResolve = mockk<(VerifyContext) -> Unit>(relaxed = true)

        runBlocking {
            resolveAttestationIdUseCase.invoke(request, metadataUrl, linkMode = true, appLink = appLink, onResolve = onResolve)
        }

        coVerify { repository.insertOrAbort(VerifyContext(1, appLink, Validation.VALID, String.Empty, null)) }
        verify { onResolve(any()) }
    }

    @Test
    fun `invoke should call resolveVerifyV2 when attestation is not empty`() {
        val metadataUrl = "https://metadata.url"
        val request = WCRequest(
            id = 1L,
            attestation = "jwt",
            method = "method",
            params = TestParams(),
            topic = Topic("topic"),
            transportType = TransportType.LINK_MODE
        )

        val result = VerifyResult(
            origin = "origin",
            validation = Validation.VALID,
            isScam = false
        )

        coEvery { verifyInterface.resolveV2(any(), any(), any(), captureLambda(), any()) } answers {
            lambda<(VerifyResult) -> Unit>().invoke(result)
        }

        coEvery { repository.insertOrAbort(any()) } just Runs

        val onResolve = mockk<(VerifyContext) -> Unit>(relaxed = true)

        runBlocking {
            resolveAttestationIdUseCase.invoke(request, metadataUrl, onResolve = onResolve)
        }

        coVerify {
            verifyInterface.resolveV2(any(), "jwt", metadataUrl, any(), any())
        }
        coVerify {
            repository.insertOrAbort(withArg { context ->
                assertEquals(request.id, context.id)
                assertEquals(result.validation, context.validation)
                assertEquals(verifyUrl, context.verifyUrl)
                assertEquals(result.isScam, context.isScam)
            })
        }
        verify { onResolve(any()) }
    }

    @Test
    fun `invoke should call resolveVerifyV2 and handle error`() {
        val request = WCRequest(
            id = 1L,
            attestation = "jwt",
            method = "method",
            params = TestParams(),
            topic = Topic("topic"),
            transportType = TransportType.LINK_MODE
        )

        val result = VerifyResult(
            origin = "origin",
            validation = Validation.VALID,
            isScam = false
        )
        val metadataUrl = "https://metadata.url"

        coEvery { verifyInterface.resolveV2(any(), any(), any(), captureLambda(), any()) } answers {
            lambda<(VerifyResult) -> Unit>().invoke(result)
        }

        coEvery { repository.insertOrAbort(any()) } just Runs

        val onResolve = mockk<(VerifyContext) -> Unit>(relaxed = true)

        runBlocking {
            resolveAttestationIdUseCase.invoke(request, metadataUrl, onResolve = onResolve)
        }

        coVerify {
            verifyInterface.resolveV2(any(), "jwt", metadataUrl, any(), any())
        }
        coVerify {
            repository.insertOrAbort(withArg { context ->
                assertEquals(request.id, context.id)
                assertEquals(Validation.VALID, context.validation)
                assertEquals(verifyUrl, context.verifyUrl)
                assertEquals(result.isScam, context.isScam)
            })
        }
        verify { onResolve(any()) }
    }

    @Test
    fun `invoke should call resolveVerifyV1 when no conditions are met`() {
        val request = WCRequest(
            id = 1L,
            attestation = null,
            method = "method",
            params = TestParams(),
            topic = Topic("topic"),
            transportType = TransportType.LINK_MODE,
            message = "message"
        )

        val result = VerifyResult(
            origin = "origin",
            validation = Validation.VALID,
            isScam = false
        )
        val metadataUrl = "https://metadata.url"

        coEvery { verifyInterface.resolve(any(), any(), any(), any()) } answers {
            thirdArg<(VerifyResult) -> Unit>().invoke(result)
        }

        coEvery { repository.insertOrAbort(any()) } just Runs

        val onResolve = mockk<(VerifyContext) -> Unit>(relaxed = true)

        runBlocking {
            resolveAttestationIdUseCase.invoke(request, metadataUrl, onResolve = onResolve)
        }

        coVerify {
            verifyInterface.resolve(sha256("message".toByteArray()), metadataUrl, any(), any())
        }
        coVerify {
            repository.insertOrAbort(withArg { context ->
                assertEquals(request.id, context.id)
                assertEquals(result.validation, context.validation)
                assertEquals(verifyUrl, context.verifyUrl)
                assertEquals(result.isScam, context.isScam)
            })
        }
        verify { onResolve(any()) }
    }

    @Test
    fun `invoke should call resolveVerifyV1 and handle error`() {
        val request = WCRequest(
            id = 1L,
            attestation = null,
            method = "method",
            params = TestParams(),
            topic = Topic("topic"),
            transportType = TransportType.LINK_MODE,
            message = "message"
        )

        val result = VerifyResult(
            origin = "origin",
            validation = Validation.UNKNOWN,
            isScam = false
        )
        val metadataUrl = "https://metadata.url"

        coEvery { verifyInterface.resolve(any(), any(), any(), any()) } answers {
            thirdArg<(VerifyResult) -> Unit>().invoke(result)
        }

        coEvery { repository.insertOrAbort(any()) } just Runs

        val onResolve = mockk<(VerifyContext) -> Unit>(relaxed = true)

        runBlocking {
            resolveAttestationIdUseCase.invoke(request, metadataUrl, onResolve = onResolve)
        }

        coVerify {
            verifyInterface.resolve(sha256("message".toByteArray()), metadataUrl, any(), any())
        }
        coVerify {
            repository.insertOrAbort(withArg { context ->
                assertEquals(request.id, context.id)
                assertEquals(Validation.UNKNOWN, context.validation)
                assertEquals(verifyUrl, context.verifyUrl)
                assertEquals(result.isScam, context.isScam)
            })
        }
        verify { onResolve(any()) }
    }
}