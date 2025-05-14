@file:JvmSynthetic

package io.crosstoken.notify.engine.calls

import io.crosstoken.android.internal.common.model.AppMetaDataType
import io.crosstoken.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import io.crosstoken.notify.common.model.Subscription
import io.crosstoken.notify.data.storage.SubscriptionRepository

internal class GetAllActiveSubscriptionsUseCase(
    private val subscriptionRepository: SubscriptionRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
) {

    suspend operator fun invoke(): Map<String, Subscription.Active> =
        subscriptionRepository.getAllActiveSubscriptions()
            .map { subscription ->
                val metadata = metadataStorageRepository.getByTopicAndType(subscription.topic, AppMetaDataType.PEER)
                subscription.copy(dappMetaData = metadata)
            }
            .associateBy { subscription -> subscription.topic.value }
}
