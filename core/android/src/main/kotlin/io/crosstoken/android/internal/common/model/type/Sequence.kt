package io.crosstoken.android.internal.common.model.type

import io.crosstoken.android.internal.common.model.Expiry
import io.crosstoken.foundation.common.model.Topic

interface Sequence {
    val topic: Topic
    val expiry: Expiry
}