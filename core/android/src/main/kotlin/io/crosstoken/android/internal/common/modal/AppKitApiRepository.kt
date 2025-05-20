package io.crosstoken.android.internal.common.modal

import android.content.Context
import io.crosstoken.android.internal.common.modal.data.model.Wallet
import io.crosstoken.android.internal.common.modal.data.model.WalletAppData
import io.crosstoken.android.internal.common.modal.data.model.WalletListing
import io.crosstoken.android.internal.common.modal.data.network.AppKitService
import io.crosstoken.android.internal.common.modal.data.network.model.WalletDTO
import io.crosstoken.android.internal.common.modal.data.network.model.WalletDataDTO
import io.crosstoken.android.utils.isWalletInstalled

internal interface AppKitApiRepositoryInterface {
    suspend fun getAndroidWalletsData(sdkType: String): Result<List<WalletAppData>>
    suspend fun getAnalyticsConfig(sdkType: String = "w3m"): Result<Boolean>
    suspend fun getWallets(
        sdkType: String,
        page: Int,
        search: String? = null,
        excludeIds: List<String>? = null,
        includeWallets: List<String>? = null
    ): Result<WalletListing>
}

internal class AppKitApiRepository(
    private val context: Context,
    private val web3ModalApiUrl: String,
    private val appKitService: AppKitService
) : AppKitApiRepositoryInterface {

    override suspend fun getAndroidWalletsData(sdkType: String) = runCatching {
        appKitService.getAndroidData(sdkType = sdkType)
    }.mapCatching { response ->
        response.body()!!.data.toWalletsAppData().filter { it.isInstalled }
    }

    override suspend fun getAnalyticsConfig(sdkType: String) = runCatching {
        appKitService.getAnalyticsConfig(sdkType = sdkType)
    }.mapCatching { response ->
        response.body()!!.isAnalyticsEnabled
    }

    override suspend fun getWallets(
        sdkType: String,
        page: Int,
        search: String?,
        excludeIds: List<String>?,
        includeWallets: List<String>?
    ) = runCatching {
        appKitService.getWallets(
            sdkType = sdkType,
            page = page,
            search = search,
            exclude = excludeIds?.joinToString(","),
            include = includeWallets?.joinToString(",")
        )
    }.mapCatching { response ->
        val body = response.body()!!
        WalletListing(
            page = page,
            totalCount = body.count,
            wallets = body.data.toWallets()
        )
    }

    private fun List<WalletDTO>.toWallets(): List<Wallet> = map { walletDTO ->
        Wallet(
            id = walletDTO.id,
            name = walletDTO.name,
            homePage = walletDTO.homePage,
            imageUrl = web3ModalApiUrl + "getWalletImage/${walletDTO.imageId}",
            order = walletDTO.order,
            mobileLink = walletDTO.mobileLink,
            playStore = walletDTO.playStore,
            webAppLink = walletDTO.webappLink,
            linkMode = walletDTO.linkMode
        ).apply {
            isWalletInstalled = context.packageManager.isWalletInstalled(appPackage)
        }
    }

    private fun List<WalletDataDTO>.toWalletsAppData() = map { data ->
        WalletAppData(
            id = data.id,
            appPackage = data.appId,
            isInstalled = context.packageManager.isWalletInstalled(data.appId)
        )
    }
}
