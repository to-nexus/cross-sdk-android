package io.crosstoken.android.internal.common.modal.domain.usecase

import io.crosstoken.android.internal.common.modal.AppKitApiRepositoryInterface

interface GetInstalledWalletsIdsUseCaseInterface {
    suspend operator fun invoke(
        sdkType: String
    ): List<String>
}

internal class GetInstalledWalletsIdsUseCase(
    private val appKitApiRepository: AppKitApiRepositoryInterface
) : GetInstalledWalletsIdsUseCaseInterface {
    override suspend fun invoke(sdkType: String): List<String> =
        appKitApiRepository.getAndroidWalletsData(sdkType).map { it.map { walletAppData -> walletAppData.id } }.getOrThrow()
}
