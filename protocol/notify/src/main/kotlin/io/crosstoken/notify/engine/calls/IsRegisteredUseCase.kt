@file:JvmSynthetic

package io.crosstoken.notify.engine.calls

import io.crosstoken.android.internal.common.model.AccountId
import io.crosstoken.android.keyserver.domain.IdentitiesInteractor
import io.crosstoken.notify.data.storage.RegisteredAccountsRepository
import io.crosstoken.notify.engine.domain.createAuthorizationReCaps

internal class IsRegisteredUseCase(
    private val registeredAccountsRepository: RegisteredAccountsRepository,
    private val identitiesInteractor: IdentitiesInteractor,
    private val identityServerUrl: String,
) : IsRegisteredUseCaseInterface {

    override suspend fun isRegistered(account: String, domain: String): Boolean {
        try {
            registeredAccountsRepository.getAccountByAccountId(account).let {
                return identitiesInteractor.getAlreadyRegisteredValidIdentity(
                    accountId = AccountId(account),
                    domain = domain,
                    resources = listOf(identityServerUrl, createAuthorizationReCaps())
                )
                    .map { true }
                    .getOrElse { false }
            }
        } catch (_: NullPointerException) {
            return false
        }
    }
}

internal interface IsRegisteredUseCaseInterface {
    suspend fun isRegistered(account: String, domain: String): Boolean
}