package io.crosstoken.appkit.domain.usecase

import io.crosstoken.appkit.domain.SessionRepository

internal class SaveChainSelectionUseCase(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(chain: String) {
        repository.updateChainSelection(chain)
    }
}