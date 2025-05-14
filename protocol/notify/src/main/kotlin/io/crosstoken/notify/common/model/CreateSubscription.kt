@file:JvmSynthetic

package io.crosstoken.notify.common.model

import io.crosstoken.android.internal.common.model.type.EngineEvent

internal sealed class CreateSubscription : EngineEvent {

    data class Success(val subscription: Subscription.Active) : CreateSubscription()

    data class Error(val throwable: Throwable) : CreateSubscription()

    object Processing : CreateSubscription()
}