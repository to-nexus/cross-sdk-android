package io.crosstoken.android.keyserver.domain.use_case

import io.crosstoken.android.keyserver.data.service.KeyServerService
import io.crosstoken.android.keyserver.model.KeyServerBody

class RegisterInviteUseCase(
    private val service: KeyServerService,
) {
    suspend operator fun invoke(idAuth: String): Result<Unit> = runCatching {
        service.registerInvite(KeyServerBody.RegisterInvite(idAuth)).unwrapUnit()
    }
}