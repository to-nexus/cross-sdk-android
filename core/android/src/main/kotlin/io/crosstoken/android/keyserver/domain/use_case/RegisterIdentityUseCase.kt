package io.crosstoken.android.keyserver.domain.use_case

import io.crosstoken.android.internal.common.signing.cacao.Cacao
import io.crosstoken.android.keyserver.data.service.KeyServerService
import io.crosstoken.android.keyserver.model.KeyServerBody

class RegisterIdentityUseCase(
    private val service: KeyServerService,
) {
    suspend operator fun invoke(cacao: Cacao): Result<Unit> = runCatching {
        service.registerIdentity(KeyServerBody.RegisterIdentity(cacao)).unwrapUnit()
    }
}