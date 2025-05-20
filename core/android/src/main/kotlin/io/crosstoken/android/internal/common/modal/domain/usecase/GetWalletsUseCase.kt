package io.crosstoken.android.internal.common.modal.domain.usecase

import io.crosstoken.android.internal.common.modal.AppKitApiRepositoryInterface
import io.crosstoken.android.internal.common.modal.data.model.WalletListing

interface GetWalletsUseCaseInterface {
    suspend operator fun invoke(
        sdkType: String,
        page: Int,
        search: String? = null,
        excludeIds: List<String>? = null,
        includeIds: List<String>? = null
    ): WalletListing
}

internal class GetWalletsUseCase(
    private val appKitApiRepository: AppKitApiRepositoryInterface
) : GetWalletsUseCaseInterface {
    override suspend fun invoke(
        sdkType: String,
        page: Int,
        search: String?,
        excludeIds: List<String>?,
        includeIds: List<String>?
    ): WalletListing = appKitApiRepository.getWallets(sdkType, page, search, excludeIds, includeIds).getOrThrow()
}
