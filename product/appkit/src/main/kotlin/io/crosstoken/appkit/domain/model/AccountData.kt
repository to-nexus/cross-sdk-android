package io.crosstoken.appkit.domain.model

import io.crosstoken.appkit.client.Modal

internal data class AccountData(
    val address: String,
    val chains: List<Modal.Model.Chain>,
    val identity: Identity? = null
)
