package io.crosstoken.android.verify.domain

import io.crosstoken.android.internal.common.model.Validation
import io.crosstoken.utils.compareDomains

fun getValidation(metadataUrl: String, origin: String) = if (compareDomains(metadataUrl, origin)) Validation.VALID else Validation.INVALID