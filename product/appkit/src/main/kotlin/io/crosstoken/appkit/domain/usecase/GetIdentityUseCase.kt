package io.crosstoken.appkit.domain.usecase

import io.crosstoken.appkit.data.BlockchainRepository
import io.crosstoken.appkit.domain.model.Identity

internal class GetIdentityUseCase(
    private val blockchainRepository: BlockchainRepository
) {
    suspend operator fun invoke(address: String, chainId: String) = try {
        blockchainRepository.getIdentity(address = address, chainId = chainId)
    } catch (e: Throwable) {
        null
    }
}
