@file:JvmSynthetic

package io.crosstoken.notify.engine.domain

import io.crosstoken.foundation.util.Logger
import io.crosstoken.notify.data.storage.RegisteredAccountsRepository
import kotlinx.coroutines.supervisorScope

internal class WatchSubscriptionsForEveryRegisteredAccountUseCase(
    private val watchSubscriptionsUseCase: WatchSubscriptionsUseCase,
    private val registeredAccountsRepository: RegisteredAccountsRepository,
    private val logger: Logger,
) {

    suspend operator fun invoke() = supervisorScope {
        val registeredAccounts = runCatching { registeredAccountsRepository.getAllAccounts() }
            .getOrElse { error -> return@supervisorScope logger.error("WatchSubscriptionsForEveryRegisteredAccountUseCase - getAllAccounts: $error") }

        registeredAccounts.forEach { registeredAccount ->
            watchSubscriptionsUseCase(registeredAccount.accountId, {}, { error -> logger.error("WatchSubscriptionsForEveryRegisteredAccountUseCase - onFailure: $registeredAccount, $error") })
        }
    }
}
