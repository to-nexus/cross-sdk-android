package io.crosstoken.notify.common.model

import io.crosstoken.android.internal.common.model.type.EngineEvent

internal data class SubscriptionChanged(
    val subscriptions: List<Subscription.Active>,
) : EngineEvent
