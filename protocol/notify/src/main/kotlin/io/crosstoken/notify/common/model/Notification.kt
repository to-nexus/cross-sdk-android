@file:JvmSynthetic

package io.crosstoken.notify.common.model

import io.crosstoken.android.internal.common.model.AppMetaData
import io.crosstoken.android.internal.common.model.type.EngineEvent

internal data class Notification(
    val id: String,
    val topic: String,
    val sentAt: Long,
    val notificationMessage: NotificationMessage,
    val metadata: AppMetaData?,
) : EngineEvent