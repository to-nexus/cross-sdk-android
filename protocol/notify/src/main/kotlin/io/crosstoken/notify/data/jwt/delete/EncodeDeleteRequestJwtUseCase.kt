@file:JvmSynthetic

package io.crosstoken.notify.data.jwt.delete

import io.crosstoken.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import io.crosstoken.android.internal.common.model.AccountId
import io.crosstoken.foundation.common.model.PublicKey
import io.crosstoken.foundation.util.jwt.encodeDidPkh
import io.crosstoken.foundation.util.jwt.encodeDidWeb
import io.crosstoken.foundation.util.jwt.encodeEd25519DidKey

internal class EncodeDeleteRequestJwtUseCase(
    private val app: String,
    private val accountId: AccountId,
    private val authenticationKey: PublicKey,
) : EncodeDidJwtPayloadUseCase<DeleteRequestJwtClaim> {

    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): DeleteRequestJwtClaim = with(params) {
        DeleteRequestJwtClaim(
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