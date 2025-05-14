@file:JvmSynthetic

package io.crosstoken.notify.engine.requests

import io.crosstoken.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import io.crosstoken.android.internal.common.model.AccountId
import io.crosstoken.android.internal.common.model.IrnParams
import io.crosstoken.android.internal.common.model.SDKError
import io.crosstoken.android.internal.common.model.Tags
import io.crosstoken.android.internal.common.model.WCRequest
import io.crosstoken.android.internal.common.model.params.ChatNotifyResponseAuthParams
import io.crosstoken.android.internal.common.model.params.CoreNotifyParams
import io.crosstoken.android.internal.common.model.type.EngineEvent
import io.crosstoken.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import io.crosstoken.android.internal.utils.fiveMinutesInSeconds
import io.crosstoken.foundation.common.model.Ttl
import io.crosstoken.foundation.util.Logger
import io.crosstoken.foundation.util.jwt.decodeDidPkh
import io.crosstoken.foundation.util.jwt.decodeEd25519DidKey
import io.crosstoken.notify.common.NotifyServerUrl
import io.crosstoken.notify.common.model.SubscriptionChanged
import io.crosstoken.notify.data.jwt.subscriptionsChanged.SubscriptionsChangedRequestJwtClaim
import io.crosstoken.notify.data.storage.RegisteredAccountsRepository
import io.crosstoken.notify.engine.domain.ExtractPublicKeysFromDidJsonUseCase
import io.crosstoken.notify.engine.domain.FetchDidJwtInteractor
import io.crosstoken.notify.engine.domain.SetActiveSubscriptionsUseCase
import io.crosstoken.notify.engine.domain.WatchSubscriptionsForEveryRegisteredAccountUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnSubscriptionsChangedUseCase(
    private val setActiveSubscriptionsUseCase: SetActiveSubscriptionsUseCase,
    private val fetchDidJwtInteractor: FetchDidJwtInteractor,
    private val extractPublicKeysFromDidJsonUseCase: ExtractPublicKeysFromDidJsonUseCase,
    private val registeredAccountsRepository: RegisteredAccountsRepository,
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val watchSubscriptionsForEveryRegisteredAccountUseCase: WatchSubscriptionsForEveryRegisteredAccountUseCase,
    private val logger: Logger,
    private val notifyServerUrl: NotifyServerUrl,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(request: WCRequest, params: CoreNotifyParams.SubscriptionsChangedParams) = supervisorScope {
        try {
            val jwtClaims = extractVerifiedDidJwtClaims<SubscriptionsChangedRequestJwtClaim>(params.subscriptionsChangedAuth).getOrThrow()
            logger.log("SubscriptionsChangedRequestJwtClaim: ${decodeEd25519DidKey(jwtClaims.audience).keyAsHex} - $jwtClaims")
            val authenticationPublicKey = registeredAccountsRepository.getAccountByIdentityKey(decodeEd25519DidKey(jwtClaims.audience).keyAsHex).notifyServerAuthenticationKey
                ?: throw IllegalStateException("Cached authentication public key is null")

            jwtClaims.throwIfIsInvalid(authenticationPublicKey.keyAsHex)

            val account = decodeDidPkh(jwtClaims.subject)
            val subscriptions = setActiveSubscriptionsUseCase(account, jwtClaims.subscriptions).getOrThrow()
            val didJwt = fetchDidJwtInteractor.subscriptionsChangedResponse(AccountId(account), authenticationPublicKey).getOrThrow()
            val responseParams = ChatNotifyResponseAuthParams.ResponseAuth(didJwt.value)
            val irnParams = IrnParams(Tags.NOTIFY_SUBSCRIPTIONS_CHANGED_RESPONSE, Ttl(fiveMinutesInSeconds))

            jsonRpcInteractor.respondWithParams(request.id, request.topic, responseParams, irnParams, onFailure = { error -> logger.error(error) })

            _events.emit(SubscriptionChanged(subscriptions))
        } catch (error: Throwable) {
            logger.error(error)
            _events.emit(SDKError(error))
        }
    }


    private suspend fun SubscriptionsChangedRequestJwtClaim.throwIfIsInvalid(expectedIssuerAsHex: String) {
        throwIfBaseIsInvalid()
        throwIfAudienceAndIssuerIsInvalidAndRetriggerWatchingLogicOnOutdatedIssuer(expectedIssuerAsHex)
    }

    private suspend fun SubscriptionsChangedRequestJwtClaim.throwIfAudienceAndIssuerIsInvalidAndRetriggerWatchingLogicOnOutdatedIssuer(expectedIssuerAsHex: String) {

        val decodedIssuerAsHex = decodeEd25519DidKey(issuer).keyAsHex
        if (decodedIssuerAsHex != expectedIssuerAsHex) {
            val (_, newAuthenticationPublicKey) = extractPublicKeysFromDidJsonUseCase(notifyServerUrl.toUri()).getOrThrow()

            if (decodedIssuerAsHex == newAuthenticationPublicKey.keyAsHex)
                watchSubscriptionsForEveryRegisteredAccountUseCase()
            else
                throw IllegalStateException("Issuer $decodedIssuerAsHex is not valid with cached $expectedIssuerAsHex or fresh ${newAuthenticationPublicKey.keyAsHex}")
        }
    }
}