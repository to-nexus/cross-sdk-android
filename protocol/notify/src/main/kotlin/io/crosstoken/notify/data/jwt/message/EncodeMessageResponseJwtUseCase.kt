@file:JvmSynthetic

package io.crosstoken.notify.data.jwt.message

import io.crosstoken.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import io.crosstoken.android.internal.common.model.AccountId
import io.crosstoken.foundation.common.model.PublicKey
import io.crosstoken.foundation.util.jwt.encodeDidPkh
import io.crosstoken.foundation.util.jwt.encodeDidWeb
import io.crosstoken.foundation.util.jwt.encodeEd25519DidKey

internal class EncodeMessageResponseJwtUseCase(
    private val app: String,
    private val accountId: AccountId,
    private val authenticationKey: PublicKey,
) : EncodeDidJwtPayloadUseCase<MessageResponseJwtClaim> {

    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): MessageResponseJwtClaim = with(params) {
        MessageResponseJwtClaim(
            issuedAt = issuedAt,
            expiration = expiration,
            issuer = issuer,
            keyserverUrl = keyserverUrl,
            audience = encodeEd25519DidKey(authenticationKey.keyAsBytes),
            subject = encodeDidPkh(accountId.value),
            app = encodeDidWeb(app)
        )
    }
}