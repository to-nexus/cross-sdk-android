package io.crosstoken.android.pairing.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.crosstoken.android.internal.common.model.type.ClientParams
import io.crosstoken.android.utils.DefaultId

sealed class PairingParams : ClientParams {

    @JsonClass(generateAdapter = true)
    class DeleteParams(
        @Json(name = "code")
        val code: Int = Int.DefaultId,
        @Json(name = "message")
        val message: String,
    ) : PairingParams()

    @Suppress("CanSealedSubClassBeObject")
    class PingParams : PairingParams()
}