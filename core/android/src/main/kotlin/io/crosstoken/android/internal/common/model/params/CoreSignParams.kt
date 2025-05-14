package io.crosstoken.android.internal.common.model.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.crosstoken.android.internal.common.model.Participant
import io.crosstoken.android.internal.common.model.RelayProtocolOptions
import io.crosstoken.android.internal.common.model.type.ClientParams
import io.crosstoken.android.internal.common.signing.cacao.Cacao

open class CoreSignParams : ClientParams {

    @JsonClass(generateAdapter = true)
    data class ApprovalParams(
        @Json(name = "relay")
        val relay: RelayProtocolOptions,
        @Json(name = "responderPublicKey")
        val responderPublicKey: String,
    ) : CoreSignParams()

    @JsonClass(generateAdapter = true)
    data class SessionAuthenticateApproveParams(
        @Json(name = "responder")
        val responder: Participant,
        @Json(name = "cacaos")
        val cacaos: List<Cacao>,
    ) : CoreSignParams() {
        val linkMode = responder.metadata.redirect?.linkMode
        val appLink = responder.metadata.redirect?.universal
    }
}