package io.crosstoken.android.keyserver.domain.use_case

import io.crosstoken.android.keyserver.data.service.KeyServerService
import io.crosstoken.android.keyserver.model.KeyServerBody

class UnregisterIdentityUseCase(
    private val service: KeyServerService,
) {
    suspend operator fun invoke(idAuth: String): Result<Unit> = runCatching {
        service.unregisterIdentity(KeyServerBody.UnregisterIdentity(idAuth)).unwrapUnit()
    }
}