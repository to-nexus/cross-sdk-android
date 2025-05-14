package io.crosstoken.android.keyserver.domain.use_case

import io.crosstoken.android.internal.common.model.AccountId
import io.crosstoken.android.keyserver.data.service.KeyServerService
import io.crosstoken.android.keyserver.model.KeyServerResponse

class ResolveInviteUseCase(
    private val service: KeyServerService
) {
    suspend operator fun invoke(accountId: AccountId): Result<KeyServerResponse.ResolveInvite> = runCatching {
        service.resolveInvite(accountId.value).unwrapValue()
    }
}