package io.crosstoken.android.internal.common.storage.metadata

import io.crosstoken.android.internal.common.model.AppMetaData
import io.crosstoken.android.internal.common.model.AppMetaDataType
import io.crosstoken.foundation.common.model.Topic

interface MetadataStorageRepositoryInterface {

    fun insertOrAbortMetadata(topic: Topic, appMetaData: AppMetaData, appMetaDataType: AppMetaDataType)

    fun updateMetaData(topic: Topic, appMetaData: AppMetaData, appMetaDataType: AppMetaDataType)

    suspend fun updateOrAbortMetaDataTopic(oldTopic: Topic, newTopic: Topic)

    fun deleteMetaData(topic: Topic)

    fun existsByTopicAndType(topic: Topic, type: AppMetaDataType): Boolean

    fun getByTopicAndType(topic: Topic, type: AppMetaDataType): AppMetaData?

    fun upsertPeerMetadata(topic: Topic, appMetaData: AppMetaData, appMetaDataType: AppMetaDataType)
}