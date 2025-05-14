package io.crosstoken.sign.client.utils

import io.crosstoken.android.cacao.signature.ISignatureType
import io.crosstoken.android.utils.cacao.CacaoSignerInterface
import io.crosstoken.sign.client.Sign

/**
 * @deprecated Only added to have backwards compatibility. Newer SDKs should only add CacaoSigner object below.
 */

@Deprecated("Moved to android-core module, as other SDKs also need CACAO.", ReplaceWith("io.crosstoken.android.internal.common.cacao.signature.SignatureType"))
enum class SignatureType(override val header: String) : ISignatureType {
    EIP191("eip191"), EIP1271("eip1271");
}

object CacaoSigner : CacaoSignerInterface<Sign.Model.Cacao.Signature>