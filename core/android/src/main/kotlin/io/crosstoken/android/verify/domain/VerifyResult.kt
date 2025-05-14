package io.crosstoken.android.verify.domain

import io.crosstoken.android.internal.common.model.Validation

data class VerifyResult(
    val validation: Validation,
    val isScam: Boolean?,
    val origin: String
)