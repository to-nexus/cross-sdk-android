package io.crosstoken.sign.engine.use_case.calls

import io.crosstoken.android.internal.common.model.AppMetaData
import io.crosstoken.android.internal.common.model.AppMetaDataType
import io.crosstoken.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import io.crosstoken.sign.engine.model.EngineDO
import io.crosstoken.sign.engine.model.mapper.toEngineDO
import io.crosstoken.sign.storage.sequence.SessionStorageRepository
import io.crosstoken.utils.isSequenceValid
import kotlinx.coroutines.supervisorScope

internal class GetSessionsUseCase(
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val sessionStorageRepository: SessionStorageRepository,
    private val selfAppMetaData: AppMetaData
) : GetSessionsUseCaseInterface {

    override suspend fun getListOfSettledSessions(): List<EngineDO.Session> = supervisorScope {
        return@supervisorScope sessionStorageRepository.getListOfSessionVOsWithoutMetadata()
            .filter { session -> session.isAcknowledged && session.expiry.isSequenceValid() }
            .map { session -> session.copy(selfAppMetaData = selfAppMetaData, peerAppMetaData = metadataStorageRepository.getByTopicAndType(session.topic, AppMetaDataType.PEER)) }
            .map { session -> session.toEngineDO() }
    }
}

internal interface GetSessionsUseCaseInterface {
    suspend fun getListOfSettledSessions(): List<EngineDO.Session>
}