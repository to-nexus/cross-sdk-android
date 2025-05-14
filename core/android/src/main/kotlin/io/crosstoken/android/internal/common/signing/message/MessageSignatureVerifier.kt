package io.crosstoken.android.internal.common.signing.message

import io.crosstoken.android.cacao.signature.SignatureType
import io.crosstoken.android.internal.common.model.ProjectId
import io.crosstoken.android.internal.common.signing.signature.Signature
import io.crosstoken.android.internal.common.signing.signature.verify


class MessageSignatureVerifier(private val projectId: ProjectId) {
    fun verify(signature: String, originalMessage: String, address: String, type: SignatureType): Boolean =
        Signature.fromString(signature).verify(originalMessage, address, type.header, projectId)
}