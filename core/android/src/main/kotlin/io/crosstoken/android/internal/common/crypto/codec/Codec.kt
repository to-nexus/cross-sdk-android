package io.crosstoken.android.internal.common.crypto.codec

import io.crosstoken.android.internal.common.model.EnvelopeType
import io.crosstoken.android.internal.common.model.Participants
import io.crosstoken.foundation.common.model.Topic

interface Codec {
    fun encrypt(topic: Topic, payload: String, envelopeType: EnvelopeType, participants: Participants? = null): ByteArray
    fun decrypt(topic: Topic, cipherText: ByteArray): String
}