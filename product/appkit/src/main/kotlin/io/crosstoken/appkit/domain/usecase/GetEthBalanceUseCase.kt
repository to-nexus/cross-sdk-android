package io.crosstoken.appkit.domain.usecase

import io.crosstoken.appkit.client.Modal
import io.crosstoken.appkit.data.BalanceRpcRepository

internal class GetEthBalanceUseCase(
    private val balanceRpcRepository: BalanceRpcRepository,
) {
    suspend operator fun invoke(
        token: Modal.Model.Token,
        rpcUrl: String,
        address: String
    ) = balanceRpcRepository.getBalance(
        token = token,
        rpcUrl = rpcUrl,
        address = address
    )
}
