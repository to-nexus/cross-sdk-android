package io.crosstoken.android.pulse.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.crosstoken.android.internal.utils.currentTimeInSeconds
import io.crosstoken.android.pulse.model.properties.Props
import io.crosstoken.util.generateId

@JsonClass(generateAdapter = true)
data class Event(
    @Json(name = "eventId")
    val eventId: Long = generateId(),
    @Json(name = "bundleId")
    val bundleId: String,
    @Json(name = "timestamp")
    val timestamp: Long = currentTimeInSeconds,
    @Json(name = "props")
    val props: Props
)