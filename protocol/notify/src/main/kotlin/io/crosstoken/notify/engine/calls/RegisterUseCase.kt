@file:JvmSynthetic

package io.crosstoken.notify.engine.calls

import io.crosstoken.android.internal.common.crypto.kmr.KeyManagementRepository
import io.crosstoken.android.internal.common.model.AccountId
import io.crosstoken.android.internal.common.model.ProjectId
import io.crosstoken.android.internal.common.signing.cacao.Cacao
import io.crosstoken.android.internal.common.signing.cacao.CacaoType
import io.crosstoken.android.internal.common.signing.cacao.CacaoVerifier
import io.crosstoken.android.internal.common.signing.cacao.Issuer
import io.crosstoken.android.keyserver.domain.IdentitiesInteractor
import io.crosstoken.notify.common.model.CacaoPayloadWithIdentityPrivateKey
import io.crosstoken.notify.data.storage.RegisteredAccountsRepository
import io.crosstoken.notify.engine.domain.WatchSubscriptionsUseCase
import kotlinx.coroutines.supervisorScope

internal class RegisterUseCase(
    private val identitiesInteractor: IdentitiesInteractor,
    private val registeredAccountsRepository: RegisteredAccountsRepository,
    private val watchSubscriptionsUseCase: WatchSubscriptionsUseCase,
    private val keyManagementRepository: KeyManagementRepository,
    private val projectId: ProjectId,
) : RegisterUseCaseInterface {

    override suspend fun register(
        cacaoPayloadWithIdentityPrivateKey: CacaoPayloadWithIdentityPrivateKey,
        signature: Cacao.Signature,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) = supervisorScope {
        val (cacaoPayload, identityPrivateKey) = cacaoPayloadWithIdentityPrivateKey
        val accountId = AccountId(Issuer(cacaoPayload.iss).accountId)

        if (!accountId.isValid())
            return@supervisorScope onFailure(IllegalArgumentException("AccountId: ${accountId.value} is not CAIP-10 compliant"))

        val identityPublicKey = runCatching { keyManagementRepository.deriveAndStoreEd25519KeyPair(identityPrivateKey) }
            .getOrElse { return@supervisorScope onFailure(IllegalArgumentException("Unable to derive identity key")) }

        runCatching { CacaoVerifier(projectId).verify(Cacao(CacaoType.EIP4361.toHeader(), cacaoPayload, signature)) }
            .getOrElse { error -> return@supervisorScope onFailure(IllegalArgumentException("Invalid signature: $error")) }

        identitiesInteractor.registerIdentity(identityPublicKey, cacaoPayload, signature).fold(
            onFailure = { error -> onFailure(error) },
            onSuccess = {
                runCatching { registeredAccountsRepository.insertOrIgnoreAccount(accountId, identityPublicKey) }.fold(
                    onFailure = { error -> onFailure(error) },
                    onSuccess = { watchSubscriptionsUseCase(accountId, onSuccess = { onSuccess(identityPublicKey.keyAsHex) }, onFailure = { error -> onFailure(error) }) }
                )
            }
        )

    }
}

internal interface RegisterUseCaseInterface {
    suspend fun register(
        cacaoPayloadWithIdentityPrivateKey: CacaoPayloadWithIdentityPrivateKey,
        signature: Cacao.Signature,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
    )
}