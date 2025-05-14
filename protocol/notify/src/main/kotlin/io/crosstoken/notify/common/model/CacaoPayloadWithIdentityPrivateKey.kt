package io.crosstoken.notify.common.model

import io.crosstoken.android.internal.common.signing.cacao.Cacao
import io.crosstoken.foundation.common.model.PrivateKey

internal data class CacaoPayloadWithIdentityPrivateKey(val payload: Cacao.Payload, val key: PrivateKey)
