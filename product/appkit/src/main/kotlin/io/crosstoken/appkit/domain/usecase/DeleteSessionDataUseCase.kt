package io.crosstoken.appkit.domain.usecase

import io.crosstoken.appkit.domain.SessionRepository

internal class DeleteSessionDataUseCase(
    private val repository: SessionRepository
) {
    suspend operator fun invoke() {
        repository.deleteSession()
    }
}