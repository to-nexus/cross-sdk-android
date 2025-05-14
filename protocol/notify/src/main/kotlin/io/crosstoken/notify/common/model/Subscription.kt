@file:JvmSynthetic

package io.crosstoken.notify.common.model

import io.crosstoken.android.internal.common.model.AccountId
import io.crosstoken.android.internal.common.model.AppMetaData
import io.crosstoken.android.internal.common.model.Expiry
import io.crosstoken.android.internal.common.model.RelayProtocolOptions
import io.crosstoken.foundation.common.model.PublicKey
import io.crosstoken.foundation.common.model.Topic

internal sealed class Subscription {
    abstract val account: AccountId
    abstract val mapOfScope: Map<String, Scope.Cached>
    abstract val expiry: Expiry

    data class Active(
        override val account: AccountId,
        override val mapOfScope: Map<String, Scope.Cached>,
        override val expiry: Expiry,
        val authenticationPublicKey: PublicKey,
        val topic: Topic,
        val dappMetaData: AppMetaData? = null,
        val requestedSubscriptionId: Long? = null,
        val relay: RelayProtocolOptions = RelayProtocolOptions(),
        val lastNotificationId: String? = null,
        val reachedEndOfHistory: Boolean = false,
    ) : Subscription()
}