package io.crosstoken.appkit.domain.usecase

import io.crosstoken.appkit.domain.SessionRepository
import kotlinx.coroutines.runBlocking

internal class GetSessionUseCase(
    private val repository: SessionRepository
) {
    operator fun invoke() = runBlocking { repository.getSession() }
}
