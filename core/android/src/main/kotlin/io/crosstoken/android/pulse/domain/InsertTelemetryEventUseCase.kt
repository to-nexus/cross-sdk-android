package io.crosstoken.android.pulse.domain

import io.crosstoken.android.internal.common.storage.events.EventsRepository
import io.crosstoken.android.pulse.model.properties.Props
import io.crosstoken.foundation.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface InsertEventUseCaseInterface {
    suspend operator fun invoke(props: Props)
}

class InsertTelemetryEventUseCase(
    private val eventsRepository: EventsRepository,
    private val logger: Logger
) : InsertEventUseCaseInterface {
    override suspend operator fun invoke(props: Props) {
        withContext(Dispatchers.IO) {
            try {
                eventsRepository.insertOrAbortTelemetry(props)
            } catch (e: Exception) {
                logger.error("Inserting event ${props.type} error: $e")
            }
        }
    }
}

class InsertEventUseCase(
    private val eventsRepository: EventsRepository,
    private val logger: Logger
) : InsertEventUseCaseInterface {
    override suspend operator fun invoke(props: Props) {
        withContext(Dispatchers.IO) {
            try {
                eventsRepository.insertOrAbort(props)
            } catch (e: Exception) {
                logger.error("Inserting event ${props.type} error: $e")
            }
        }
    }
}