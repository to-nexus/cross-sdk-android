package io.crosstoken.appkit.domain.usecase

import io.crosstoken.appkit.domain.RecentWalletsRepository

internal class SaveRecentWalletUseCase(
    private val repository: RecentWalletsRepository
) {
    operator fun invoke(id: String) = repository.saveRecentWalletId(id)
}