@file:JvmSynthetic

package io.crosstoken.notify.engine.calls

import io.crosstoken.android.internal.common.model.AccountId
import io.crosstoken.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import io.crosstoken.android.keyserver.domain.IdentitiesInteractor
import io.crosstoken.foundation.common.model.Topic
import io.crosstoken.notify.data.storage.NotificationsRepository
import io.crosstoken.notify.data.storage.RegisteredAccountsRepository
import io.crosstoken.notify.data.storage.SubscriptionRepository
import io.crosstoken.notify.engine.domain.StopWatchingSubscriptionsUseCase
import kotlinx.coroutines.supervisorScope

internal class UnregisterUseCase(
    private val identitiesInteractor: IdentitiesInteractor,
    private val keyserverUrl: String,
    private val registeredAccountsRepository: RegisteredAccountsRepository,
    private val stopWatchingSubscriptionsUseCase: StopWatchingSubscriptionsUseCase,
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val subscriptionRepository: SubscriptionRepository,
    private val notificationsRepository: NotificationsRepository,
) : UnregisterUseCaseInterface {

    override suspend fun unregister(
        account: String,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) = supervisorScope {
        val accountId = AccountId(account)
        identitiesInteractor.unregisterIdentity(accountId, keyserverUrl).fold(
            onFailure = { error -> onFailure(error) },
            onSuccess = { identityPublicKey ->
                runCatching {
                    stopWatchingSubscriptionsUseCase(accountId, onFailure = { error -> onFailure(error) })
                    registeredAccountsRepository.deleteAccountByAccountId(account)
                }.fold(
                    onFailure = { error -> onFailure(error) },
                    onSuccess = {
                        subscriptionRepository.getAccountActiveSubscriptions(accountId).map { it.topic.value }.map { topic ->
                            jsonRpcInteractor.unsubscribe(Topic(topic)) { error -> onFailure(error) }
                            subscriptionRepository.deleteSubscriptionByNotifyTopic(topic)
                            notificationsRepository.deleteNotificationsByTopic(topic)
                        }
                        onSuccess(identityPublicKey.keyAsHex)
                    }
                )
            }
        )
    }
}

internal interface UnregisterUseCaseInterface {
    suspend fun unregister(
        account: String,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
    )
}