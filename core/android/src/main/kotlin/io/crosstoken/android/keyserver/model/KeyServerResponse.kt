package io.crosstoken.android.keyserver.model

import com.squareup.moshi.JsonClass
import io.crosstoken.android.internal.common.signing.cacao.Cacao

sealed class KeyServerResponse {

    @JsonClass(generateAdapter = true)
    data class ResolveInvite(val inviteKey: String) : KeyServerResponse()

    @JsonClass(generateAdapter = true)
    data class ResolveIdentity(val cacao: Cacao) : KeyServerResponse()
}