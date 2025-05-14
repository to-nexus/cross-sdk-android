@file:JvmSynthetic

package io.crosstoken.notify.engine.responses

import io.crosstoken.android.internal.common.JsonRpcResponse
import io.crosstoken.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import io.crosstoken.android.internal.common.model.WCResponse
import io.crosstoken.android.internal.common.model.params.ChatNotifyResponseAuthParams
import io.crosstoken.android.internal.common.model.params.CoreNotifyParams
import io.crosstoken.android.internal.common.model.type.EngineEvent
import io.crosstoken.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import io.crosstoken.foundation.util.Logger
import io.crosstoken.foundation.util.jwt.decodeDidPkh
import io.crosstoken.notify.common.model.DeleteSubscription
import io.crosstoken.notify.data.jwt.delete.DeleteResponseJwtClaim
import io.crosstoken.notify.data.storage.NotificationsRepository
import io.crosstoken.notify.engine.domain.SetActiveSubscriptionsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnDeleteResponseUseCase(
    private val setActiveSubscriptionsUseCase: SetActiveSubscriptionsUseCase,
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val notificationsRepository: NotificationsRepository,
    private val logger: Logger,
) {
    private val _events: MutableSharedFlow<Pair<CoreNotifyParams.DeleteParams, EngineEvent>> = MutableSharedFlow()
    val events: SharedFlow<Pair<CoreNotifyParams.DeleteParams, EngineEvent>> = _events.asSharedFlow()

    suspend operator fun invoke(wcResponse: WCResponse, params: CoreNotifyParams.DeleteParams) = supervisorScope {
        val resultEvent = try {
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val responseAuth = (response.result as ChatNotifyResponseAuthParams.ResponseAuth).responseAuth
                    val responseJwtClaim = extractVerifiedDidJwtClaims<DeleteResponseJwtClaim>(responseAuth).getOrThrow()
                    responseJwtClaim.throwIfBaseIsInvalid()

                    jsonRpcInteractor.unsubscribe(wcResponse.topic)

                    notificationsRepository.deleteNotificationsByTopic(wcResponse.topic.value)
                    setActiveSubscriptionsUseCase(decodeDidPkh(responseJwtClaim.subject), responseJwtClaim.subscriptions).getOrThrow()

                    DeleteSubscription.Success(wcResponse.topic.value)
                }

                is JsonRpcResponse.JsonRpcError -> DeleteSubscription.Error(Throwable(response.error.message))
            }
        } catch (e: Exception) {
            logger.error(e)
            DeleteSubscription.Error(e)
        }

        _events.emit(params to resultEvent)
    }
}