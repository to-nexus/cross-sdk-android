@file:JvmSynthetic

package io.crosstoken.notify.common.model

import io.crosstoken.android.internal.common.model.type.EngineEvent

internal data class Error(
    val requestId: Long,
    val rejectionReason: String,
) : EngineEvent