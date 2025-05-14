@file:JvmSynthetic

package io.crosstoken.notify.common.model

import io.crosstoken.android.internal.common.model.type.EngineEvent

internal sealed class DeleteSubscription : EngineEvent {

    data class Success(val topic: String) : DeleteSubscription()

    data class Error(val throwable: Throwable) : DeleteSubscription()

    object Processing : DeleteSubscription()
}