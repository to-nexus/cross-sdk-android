package io.crosstoken.appkit.domain.usecase

import io.crosstoken.appkit.domain.SessionRepository

internal class ObserveSessionUseCase(
    private val repository: SessionRepository
) {
    operator fun invoke() = repository.session
}