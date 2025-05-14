@file:JvmSynthetic

package io.crosstoken.notify.data.jwt.subscription

import io.crosstoken.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import io.crosstoken.android.internal.common.model.AccountId
import io.crosstoken.foundation.common.model.PublicKey
import io.crosstoken.foundation.util.jwt.encodeDidPkh
import io.crosstoken.foundation.util.jwt.encodeDidWeb
import io.crosstoken.foundation.util.jwt.encodeEd25519DidKey

internal class EncodeSubscriptionRequestJwtUseCase(
    private val app: String,
    private val accountId: AccountId,
    private val authenticationKey: PublicKey,
    private val scope: String
) : EncodeDidJwtPayloadUseCase<SubscriptionRequestJwtClaim> {

    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): SubscriptionRequestJwtClaim = with(params) {
        SubscriptionRequestJwtClaim(
            issuedAt = issuedAt,
            expiration = expiration,
            issuer = issuer,
            keyserverUrl = keyserverUrl,
            audience = encodeEd25519DidKey(authenticationKey.keyAsBytes),
            subject = encodeDidPkh(accountId.value),
            scope = scope,
            app = encodeDidWeb(app)
        )
    }
}