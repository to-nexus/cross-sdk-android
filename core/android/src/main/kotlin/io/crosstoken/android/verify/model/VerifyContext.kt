package io.crosstoken.android.verify.model

import io.crosstoken.android.internal.common.model.Validation

data class VerifyContext(
    val id: Long,
    val origin: String,
    val validation: Validation,
    val verifyUrl: String,
    val isScam: Boolean?
)