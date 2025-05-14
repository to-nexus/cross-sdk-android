package io.crosstoken.appkit.data

import io.crosstoken.foundation.util.Logger
import io.crosstoken.appkit.client.Modal
import io.crosstoken.appkit.data.json_rpc.balance.BalanceRequest
import io.crosstoken.appkit.data.json_rpc.balance.BalanceRpcResponse
import io.crosstoken.appkit.data.network.BalanceService
import io.crosstoken.appkit.domain.model.Balance

internal class BalanceRpcRepository(
    private val balanceService: BalanceService,
    private val logger: Logger,
) {

    suspend fun getBalance(
        token: Modal.Model.Token, rpcUrl: String, address: String
    ) = runCatching {
        balanceService.getBalance(
            url = rpcUrl, body = BalanceRequest(address = address)
        )
    }.mapCatching { response ->
        response.body()!!.mapResponse(token)
    }.onFailure {
        logger.error(it)
    }.getOrNull()
}

private fun BalanceRpcResponse.mapResponse(token: Modal.Model.Token) = when {
    result != null -> Balance(token, result)
    error != null -> throw Throwable(error.message)
    else -> throw Throwable("Invalid balance response")
}
