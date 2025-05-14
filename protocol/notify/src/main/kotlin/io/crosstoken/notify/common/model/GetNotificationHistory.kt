@file:JvmSynthetic

package io.crosstoken.notify.common.model

import io.crosstoken.android.internal.common.model.type.EngineEvent

internal sealed class GetNotificationHistory : EngineEvent {

    data class Success(val notifications: List<Notification>, val hasMore: Boolean) : GetNotificationHistory()

    data class Error(val throwable: Throwable) : GetNotificationHistory()

    object Processing : GetNotificationHistory()
}