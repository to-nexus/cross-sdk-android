package io.crosstoken.appkit.domain.usecase

import io.crosstoken.appkit.domain.SessionRepository
import io.crosstoken.appkit.domain.model.Session

internal class SaveSessionUseCase(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(session: Session) = repository.saveSession(session)
}