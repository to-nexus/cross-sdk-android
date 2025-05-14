@file:JvmSynthetic

package io.crosstoken.notify.engine.domain

import io.crosstoken.android.internal.common.crypto.kmr.KeyManagementRepository
import io.crosstoken.android.internal.common.model.AccountId
import io.crosstoken.android.internal.utils.getPeerTag
import io.crosstoken.foundation.common.model.Topic

internal class GetSelfKeyForWatchSubscriptionUseCase(
    private val keyManagementRepository: KeyManagementRepository,
) {
    suspend operator fun invoke(requestTopic: Topic, accountId: AccountId) = runCatching {
        keyManagementRepository.getPublicKey(Pair(accountId, requestTopic).getPeerTag())
    }.getOrElse {
        keyManagementRepository.generateAndStoreX25519KeyPair().also { pubKey ->
            keyManagementRepository.setKey(pubKey, Pair(accountId, requestTopic).getPeerTag())
        }
    }
}
