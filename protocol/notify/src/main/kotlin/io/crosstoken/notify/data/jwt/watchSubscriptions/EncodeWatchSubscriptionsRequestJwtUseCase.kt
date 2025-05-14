@file:JvmSynthetic

package io.crosstoken.notify.data.jwt.watchSubscriptions

import io.crosstoken.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import io.crosstoken.android.internal.common.model.AccountId
import io.crosstoken.foundation.common.model.PublicKey
import io.crosstoken.foundation.util.jwt.encodeDidPkh
import io.crosstoken.foundation.util.jwt.encodeDidWeb
import io.crosstoken.foundation.util.jwt.encodeEd25519DidKey

internal class EncodeWatchSubscriptionsRequestJwtUseCase(
    private val accountId: AccountId,
    private val authenticationKey: PublicKey,
    private val appDomain: String?,
) : EncodeDidJwtPayloadUseCase<WatchSubscriptionsRequestJwtClaim> {

    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): WatchSubscriptionsRequestJwtClaim = with(params) {
        WatchSubscriptionsRequestJwtClaim(
            issuedAt = issuedAt,
            expiration = expiration,
            issuer = issuer,
            keyserverUrl = keyserverUrl,
            audience = encodeEd25519DidKey(authenticationKey.keyAsBytes),
            subject = encodeDidPkh(accountId.value),
            appDidWeb = appDomain?.let { encodeDidWeb(it) }
        )
    }
}