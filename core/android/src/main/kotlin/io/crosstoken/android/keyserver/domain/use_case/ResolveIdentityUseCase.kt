package io.crosstoken.android.keyserver.domain.use_case

import io.crosstoken.android.keyserver.data.service.KeyServerService
import io.crosstoken.android.keyserver.model.KeyServerResponse

class ResolveIdentityUseCase(
    private val service: KeyServerService,
) {
    suspend operator fun invoke(identityKey: String): Result<KeyServerResponse.ResolveIdentity> = runCatching {
        service.resolveIdentity(identityKey).unwrapValue()
    }
}