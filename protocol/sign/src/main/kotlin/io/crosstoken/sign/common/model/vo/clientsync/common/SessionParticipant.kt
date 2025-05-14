@file:JvmSynthetic

package io.crosstoken.sign.common.model.vo.clientsync.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.crosstoken.android.internal.common.model.AppMetaData

@JsonClass(generateAdapter = true)
internal data class SessionParticipant(
    @Json(name = "publicKey")
    val publicKey: String,
    @Json(name = "metadata")
    val metadata: AppMetaData,
)
