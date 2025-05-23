package io.crosstoken.android.internal.common.modal.domain.usecase

import io.crosstoken.android.internal.common.modal.AppKitApiRepositoryInterface
import kotlinx.coroutines.runBlocking

interface EnableAnalyticsUseCaseInterface {
    fun fetchAnalyticsConfig(): Boolean
}

internal class EnableAnalyticsUseCase(private val repository: AppKitApiRepositoryInterface) :
    EnableAnalyticsUseCaseInterface {
    override fun fetchAnalyticsConfig(): Boolean {
        return runBlocking {
            try {
                val response = repository.getAnalyticsConfig()
                if (response.isSuccess) {
                    response.getOrDefault(false)
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }
}