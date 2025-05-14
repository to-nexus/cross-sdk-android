package io.crosstoken.sign.test.utils.hybrid

import io.crosstoken.android.Core
import io.crosstoken.sign.client.Sign
import io.crosstoken.sign.test.utils.TestClient
import io.crosstoken.sign.test.utils.globalOnError
import io.crosstoken.sign.test.utils.proposalNamespaces
import timber.log.Timber

val HybridSignClient = TestClient.Hybrid.signClient

val hybridClientConnect = { pairing: Core.Model.Pairing ->
    val connectParams = Sign.Params.Connect(namespaces = proposalNamespaces, optionalNamespaces = null, properties = null, pairing = pairing)
    HybridSignClient.connect(
        connectParams,
        onSuccess = { url -> Timber.d("HybridDappClient: connect onSuccess, url: $url") },
        onError = ::globalOnError
    )
}