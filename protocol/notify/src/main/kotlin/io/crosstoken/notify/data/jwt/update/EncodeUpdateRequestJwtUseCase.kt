@file:JvmSynthetic

package io.crosstoken.notify.data.jwt.update

import io.crosstoken.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import io.crosstoken.android.internal.common.model.AccountId
import io.crosstoken.foundation.common.model.PublicKey
import io.crosstoken.foundation.util.jwt.encodeDidPkh
import io.crosstoken.foundation.util.jwt.encodeDidWeb
import io.crosstoken.foundation.util.jwt.encodeEd25519DidKey

internal class EncodeUpdateRequestJwtUseCase(
    private val accountId: AccountId,
    private val app: String,
    private val authenticationKey: PublicKey,
    private val scope: String,
) : EncodeDidJwtPayloadUseCase<UpdateRequestJwtClaim> {

    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): UpdateRequestJwtClaim = with(params) {
        UpdateRequestJwtClaim(
            issuedAt = issuedAt,
            expiration = expiration,
            issuer = issuer,
            keyserverUrl = keyserverUrl,
            audience = encodeEd25519DidKey(authenticationKey.keyAsBytes),
            subject = encodeDidPkh(accountId.value),
            app = encodeDidWeb(app),
            scope = scope,
        )
    }
}