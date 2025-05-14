@file:JvmSynthetic

package io.crosstoken.notify.engine.responses

import io.crosstoken.android.internal.common.JsonRpcResponse
import io.crosstoken.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import io.crosstoken.android.internal.common.model.WCResponse
import io.crosstoken.android.internal.common.model.params.ChatNotifyResponseAuthParams
import io.crosstoken.android.internal.common.model.params.CoreNotifyParams
import io.crosstoken.android.internal.common.model.type.EngineEvent
import io.crosstoken.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import io.crosstoken.foundation.common.model.Topic
import io.crosstoken.foundation.util.Logger
import io.crosstoken.foundation.util.jwt.decodeDidPkh
import io.crosstoken.notify.common.model.CreateSubscription
import io.crosstoken.notify.data.jwt.subscription.SubscriptionRequestJwtClaim
import io.crosstoken.notify.data.jwt.subscription.SubscriptionResponseJwtClaim
import io.crosstoken.notify.data.storage.SubscriptionRepository
import io.crosstoken.notify.engine.domain.FindRequestedSubscriptionUseCase
import io.crosstoken.notify.engine.domain.SetActiveSubscriptionsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnSubscribeResponseUseCase(
    private val setActiveSubscriptionsUseCase: SetActiveSubscriptionsUseCase,
    private val findRequestedSubscriptionUseCase: FindRequestedSubscriptionUseCase,
    private val subscriptionRepository: SubscriptionRepository,
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val logger: Logger,
) {
    private val _events: MutableSharedFlow<Pair<CoreNotifyParams.SubscribeParams, EngineEvent>> = MutableSharedFlow()
    val events: SharedFlow<Pair<CoreNotifyParams.SubscribeParams, EngineEvent>> = _events.asSharedFlow()


    suspend operator fun invoke(wcResponse: WCResponse, params: CoreNotifyParams.SubscribeParams) = supervisorScope {
        jsonRpcInteractor.unsubscribe(wcResponse.topic)

        val resultEvent = try {
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val responseAuth = (response.result as ChatNotifyResponseAuthParams.ResponseAuth).responseAuth
                    val responseJwtClaim = extractVerifiedDidJwtClaims<SubscriptionResponseJwtClaim>(responseAuth).getOrThrow()
                    responseJwtClaim.throwIfBaseIsInvalid()

                    val subscriptions = setActiveSubscriptionsUseCase(decodeDidPkh(responseJwtClaim.subject), responseJwtClaim.subscriptions).getOrThrow()
                    val requestJwtClaim = extractVerifiedDidJwtClaims<SubscriptionRequestJwtClaim>(params.subscriptionAuth).getOrThrow()
                    val subscription = findRequestedSubscriptionUseCase(requestJwtClaim.audience, subscriptions)

                    CreateSubscription.Success(subscription)
                }

                is JsonRpcResponse.JsonRpcError -> {
                    removeOptimisticallyAddedSubscription(wcResponse.topic)
                    CreateSubscription.Error(Throwable(response.error.message))
                }

            }
        } catch (e: Exception) {
            removeOptimisticallyAddedSubscription(wcResponse.topic)
            logger.error(e)
            CreateSubscription.Error(e)
        }

        _events.emit(params to resultEvent)
    }


    private suspend fun removeOptimisticallyAddedSubscription(topic: Topic) =
        runCatching { subscriptionRepository.deleteSubscriptionByNotifyTopic(topic.value) }.getOrElse { logger.error("OnSubscribeResponse - Error - No subscription found for removal") }
}