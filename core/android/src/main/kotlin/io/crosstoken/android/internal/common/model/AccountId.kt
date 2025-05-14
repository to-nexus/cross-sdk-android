@file:JvmSynthetic

package io.crosstoken.android.internal.common.model

import io.crosstoken.android.internal.utils.CoreValidator


@JvmInline
value class AccountId(val value: String) {
    fun isValid(): Boolean = CoreValidator.isAccountIdCAIP10Compliant(value)
    fun address() = value.split(":").last()
}