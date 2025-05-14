@file:JvmName("Expiration")

package io.crosstoken.android.pairing.model

import io.crosstoken.android.internal.utils.currentTimeInSeconds
import io.crosstoken.android.internal.utils.fiveMinutesInSeconds

val pairingExpiry: Long get() = currentTimeInSeconds + fiveMinutesInSeconds