@file:JvmSynthetic

package io.crosstoken.notify.common.model

import io.crosstoken.android.internal.common.model.type.EngineEvent

internal sealed class UpdateSubscription : EngineEvent {

    data class Success(val subscription: Subscription.Active) : UpdateSubscription()

    data class Error(val throwable: Throwable) : UpdateSubscription()

    object Processing : UpdateSubscription()
}