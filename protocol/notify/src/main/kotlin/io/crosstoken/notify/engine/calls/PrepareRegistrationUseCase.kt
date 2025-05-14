@file:JvmSynthetic

package io.crosstoken.notify.engine.calls

import io.crosstoken.android.internal.common.crypto.kmr.KeyManagementRepository
import io.crosstoken.android.internal.common.model.AccountId
import io.crosstoken.android.internal.common.signing.cacao.toCAIP222Message
import io.crosstoken.android.keyserver.domain.IdentitiesInteractor
import io.crosstoken.foundation.util.Logger
import io.crosstoken.notify.common.model.CacaoPayloadWithIdentityPrivateKey
import io.crosstoken.notify.engine.domain.createAuthorizationReCaps

internal class PrepareRegistrationUseCase(
    private val identitiesInteractor: IdentitiesInteractor,
    private val identityServerUrl: String,
    private val keyManagementRepository: KeyManagementRepository,
    private val logger: Logger
) : PrepareRegistrationUseCaseInterface {

    override suspend fun prepareRegistration(
        account: String,
        domain: String,
        onSuccess: (CacaoPayloadWithIdentityPrivateKey, String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val identityPublicKey = identitiesInteractor.generateAndStoreIdentityKeyPair()
        val (_, identityPrivateKey) = keyManagementRepository.getKeyPair(identityPublicKey)

        identitiesInteractor.generatePayload(
            AccountId(account),
            identityPublicKey,
            null,
            domain,
            listOf(identityServerUrl, createAuthorizationReCaps())
        )
            .also {
                runCatching {
                    keyManagementRepository.removeKeys(identityPublicKey.keyAsHex)
                }.onFailure { logger.error(it) }
            }
            .fold(
                onFailure = { error -> onFailure(error) },
                onSuccess = { cacaoPayload ->
                    onSuccess(
                        CacaoPayloadWithIdentityPrivateKey(
                            cacaoPayload,
                            identityPrivateKey
                        ),
                        cacaoPayload.toCAIP222Message()
                    )
                }
            )
    }
}

internal interface PrepareRegistrationUseCaseInterface {
    suspend fun prepareRegistration(
        account: String,
        domain: String,
        onSuccess: (CacaoPayloadWithIdentityPrivateKey, String) -> Unit,
        onFailure: (Throwable) -> Unit,
    )
}