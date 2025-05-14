@file:JvmSynthetic

package io.crosstoken.notify.data.jwt.getNotifications

import io.crosstoken.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import io.crosstoken.android.internal.common.model.AccountId
import io.crosstoken.foundation.common.model.PublicKey
import io.crosstoken.foundation.util.jwt.encodeDidPkh
import io.crosstoken.foundation.util.jwt.encodeDidWeb
import io.crosstoken.foundation.util.jwt.encodeEd25519DidKey

internal class EncodeGetNotificationsRequestJwtUseCase(
    private val app: String,
    private val accountId: AccountId,
    private val authenticationKey: PublicKey,
    private val limit: Int,
    private val after: String?,
) : EncodeDidJwtPayloadUseCase<GetNotificationsRequestJwtClaim> {

    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): GetNotificationsRequestJwtClaim = with(params) {
        GetNotificationsRequestJwtClaim(
            issuedAt = issuedAt,
            expiration = expiration,
            issuer = issuer,
            keyserverUrl = keyserverUrl,
            audience = encodeEd25519DidKey(authenticationKey.keyAsBytes),
            subject = encodeDidPkh(accountId.value),
            app = encodeDidWeb(app),
            limit = limit,
            after = after,
        )
    }
}