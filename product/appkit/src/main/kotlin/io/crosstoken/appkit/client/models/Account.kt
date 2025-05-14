package io.crosstoken.appkit.client.models

import io.crosstoken.appkit.client.Modal

data class Account(
    val address: String,
    val chain: Modal.Model.Chain
)