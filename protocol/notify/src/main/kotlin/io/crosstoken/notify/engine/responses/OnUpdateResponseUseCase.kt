@file:JvmSynthetic

package io.crosstoken.notify.engine.responses

import io.crosstoken.android.internal.common.JsonRpcResponse
import io.crosstoken.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import io.crosstoken.android.internal.common.model.SDKError
import io.crosstoken.android.internal.common.model.WCResponse
import io.crosstoken.android.internal.common.model.params.ChatNotifyResponseAuthParams
import io.crosstoken.android.internal.common.model.params.CoreNotifyParams
import io.crosstoken.android.internal.common.model.type.EngineEvent
import io.crosstoken.foundation.util.Logger
import io.crosstoken.foundation.util.jwt.decodeDidPkh
import io.crosstoken.notify.common.model.CreateSubscription
import io.crosstoken.notify.common.model.UpdateSubscription
import io.crosstoken.notify.data.jwt.update.UpdateRequestJwtClaim
import io.crosstoken.notify.data.jwt.update.UpdateResponseJwtClaim
import io.crosstoken.notify.engine.domain.FindRequestedSubscriptionUseCase
import io.crosstoken.notify.engine.domain.SetActiveSubscriptionsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnUpdateResponseUseCase(
    private val setActiveSubscriptionsUseCase: SetActiveSubscriptionsUseCase,
    private val findRequestedSubscriptionUseCase: FindRequestedSubscriptionUseCase,
    private val logger: Logger
) {
    private val _events: MutableSharedFlow<Pair<CoreNotifyParams.UpdateParams, EngineEvent>> = MutableSharedFlow()
    val events: SharedFlow<Pair<CoreNotifyParams.UpdateParams, EngineEvent>> = _events.asSharedFlow()

    suspend operator fun invoke(wcResponse: WCResponse, params: CoreNotifyParams.UpdateParams) = supervisorScope {
        val resultEvent = try {
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val responseAuth = (response.result as ChatNotifyResponseAuthParams.ResponseAuth).responseAuth
                    val responseJwtClaim = extractVerifiedDidJwtClaims<UpdateResponseJwtClaim>(responseAuth).getOrThrow()
                    responseJwtClaim.throwIfBaseIsInvalid()

                    val subscriptions = setActiveSubscriptionsUseCase(decodeDidPkh(responseJwtClaim.subject), responseJwtClaim.subscriptions).getOrThrow()
                    val requestJwtClaim = extractVerifiedDidJwtClaims<UpdateRequestJwtClaim>(params.updateAuth).getOrThrow()
                    val subscription = findRequestedSubscriptionUseCase(requestJwtClaim.audience, subscriptions)

                    UpdateSubscription.Success(subscription)
                }

                is JsonRpcResponse.JsonRpcError -> UpdateSubscription.Error(Throwable(response.error.message))
            }
        } catch (e: Exception) {
            logger.error(e)
            CreateSubscription.Error(e)
        }

        _events.emit(params to resultEvent)
    }
}