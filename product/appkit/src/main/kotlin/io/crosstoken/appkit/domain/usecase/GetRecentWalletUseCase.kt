package io.crosstoken.appkit.domain.usecase

import io.crosstoken.appkit.domain.RecentWalletsRepository

internal class GetRecentWalletUseCase(private val repository: RecentWalletsRepository) {
    operator fun invoke() = repository.getRecentWalletId()
}