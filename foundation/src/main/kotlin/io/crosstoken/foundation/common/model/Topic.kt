package io.crosstoken.foundation.common.model

import com.squareup.moshi.JsonClass
import io.crosstoken.util.Empty

@JsonClass(generateAdapter = false)
data class Topic(val value: String = String.Empty)