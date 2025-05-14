package io.crosstoken.notify.common.model

import io.crosstoken.foundation.common.model.Topic

internal sealed interface TimeoutInfo {
    data class Data(val requestId: Long, val topic: Topic) : TimeoutInfo
    object Nothing : TimeoutInfo
}