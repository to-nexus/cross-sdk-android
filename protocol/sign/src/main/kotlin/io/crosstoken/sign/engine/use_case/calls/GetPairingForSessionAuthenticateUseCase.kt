package io.crosstoken.sign.engine.use_case.calls

import io.crosstoken.android.Core
import io.crosstoken.android.pairing.client.PairingInterface
import io.crosstoken.sign.json_rpc.model.JsonRpcMethod

internal class GetPairingForSessionAuthenticateUseCase(private var pairingProtocol: PairingInterface) {
    operator fun invoke(pairingTopic: String?): Core.Model.Pairing {
        val pairing: Core.Model.Pairing = if (pairingTopic != null) {
            pairingProtocol.getPairings().find { pairing -> pairing.topic == pairingTopic } ?: throw Exception("Pairing does not exist")
        } else {
            pairingProtocol.create(methods = JsonRpcMethod.WC_SESSION_AUTHENTICATE, onError = { error -> throw error.throwable }) ?: throw Exception("Cannot create a pairing")
        }
        if (!pairing.registeredMethods.contains(JsonRpcMethod.WC_SESSION_AUTHENTICATE)) {
            throw Exception("Pairing does not support wc_sessionAuthenticate")
        }
        return pairing
    }
}