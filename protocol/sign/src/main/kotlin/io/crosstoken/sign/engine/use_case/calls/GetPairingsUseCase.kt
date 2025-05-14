package io.crosstoken.sign.engine.use_case.calls

import io.crosstoken.android.pairing.client.PairingInterface
import io.crosstoken.android.pairing.model.mapper.toPairing
import io.crosstoken.sign.engine.model.EngineDO
import kotlinx.coroutines.supervisorScope

internal class GetPairingsUseCase(private val pairingInterface: PairingInterface) : GetPairingsUseCaseInterface {

    override suspend fun getListOfSettledPairings(): List<EngineDO.PairingSettle> = supervisorScope {
        return@supervisorScope pairingInterface.getPairings().map { pairing ->
            val mappedPairing = pairing.toPairing()
            EngineDO.PairingSettle(mappedPairing.topic, mappedPairing.peerAppMetaData)
        }
    }
}

internal interface GetPairingsUseCaseInterface {
    suspend fun getListOfSettledPairings(): List<EngineDO.PairingSettle>
}