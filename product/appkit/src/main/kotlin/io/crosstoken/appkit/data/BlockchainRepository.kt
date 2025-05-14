package io.crosstoken.appkit.data

import io.crosstoken.android.internal.common.model.ProjectId
import io.crosstoken.appkit.data.model.IdentityDTO
import io.crosstoken.appkit.data.network.BlockchainService
import io.crosstoken.appkit.domain.model.Identity

internal class BlockchainRepository(
    private val blockchainService: BlockchainService,
    private val projectId: ProjectId
) {

    suspend fun getIdentity(address: String, chainId: String) = with(
        blockchainService.getIdentity(address = address, chainId = chainId, projectId = projectId.value)
    ) {
        if (isSuccessful && body() != null) {
            body()!!.toIdentity()
        } else {
            throw Throwable(errorBody()?.string())
        }
    }
}

private fun IdentityDTO.toIdentity() = Identity(name, avatar)
