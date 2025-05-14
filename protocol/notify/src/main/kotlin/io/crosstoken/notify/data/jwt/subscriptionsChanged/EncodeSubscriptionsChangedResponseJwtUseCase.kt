@file:JvmSynthetic

package io.crosstoken.notify.data.jwt.subscriptionsChanged

import io.crosstoken.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import io.crosstoken.android.internal.common.model.AccountId
import io.crosstoken.foundation.common.model.PublicKey
import io.crosstoken.foundation.util.jwt.encodeDidPkh
import io.crosstoken.foundation.util.jwt.encodeEd25519DidKey

internal class EncodeSubscriptionsChangedResponseJwtUseCase(
    private val accountId: AccountId,
    private val authenticationKey: PublicKey,
) : EncodeDidJwtPayloadUseCase<SubscriptionsChangedResponseJwtClaim> {

    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): SubscriptionsChangedResponseJwtClaim = with(params) {
        SubscriptionsChangedResponseJwtClaim(
            issuedAt = issuedAt,
            expiration = expiration,
            issuer = issuer,
            keyserverUrl = keyserverUrl,
            audience = encodeEd25519DidKey(authenticationKey.keyAsBytes),
            subject = encodeDidPkh(accountId.value),
        )
    }
}