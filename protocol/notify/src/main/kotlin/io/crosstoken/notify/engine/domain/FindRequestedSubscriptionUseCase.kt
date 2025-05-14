package io.crosstoken.notify.engine.domain

import io.crosstoken.android.internal.common.model.AppMetaDataType
import io.crosstoken.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import io.crosstoken.foundation.util.jwt.encodeEd25519DidKey
import io.crosstoken.notify.common.model.Subscription
import kotlinx.coroutines.supervisorScope

internal class FindRequestedSubscriptionUseCase(
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
) {

    suspend operator fun invoke(encodedAuthenticationPublicKey: String, subscriptions: List<Subscription.Active>): Subscription.Active = supervisorScope {
        val subscription = subscriptions.firstOrNull { encodeEd25519DidKey(it.authenticationPublicKey.keyAsBytes) == encodedAuthenticationPublicKey }
            ?: throw Exception("No subscription found for audience $encodedAuthenticationPublicKey")

        val metadata = metadataStorageRepository.getByTopicAndType(subscription.topic, AppMetaDataType.PEER)
        return@supervisorScope subscription.copy(dappMetaData = metadata)
    }

}