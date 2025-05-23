package io.crosstoken.android.internal.common.modal

import android.content.Context
import io.crosstoken.android.BuildConfig
import io.crosstoken.android.internal.common.modal.data.model.Wallet
import io.crosstoken.android.internal.common.modal.data.model.WalletAppData
import io.crosstoken.android.internal.common.modal.data.model.WalletListing
import io.crosstoken.android.utils.isWalletInstalled

internal class AppKitCrossApiRepository(
    private val context: Context
) : AppKitApiRepositoryInterface {

    override suspend fun getAndroidWalletsData(sdkType: String) = Result.success(wallets.map { wallet ->
        WalletAppData(
            id = wallet.id,
            appPackage = wallet.appPackage,
            isInstalled = context.packageManager.isWalletInstalled(wallet.appPackage),
        )
    }.filter { it.isInstalled })

    override suspend fun getAnalyticsConfig(sdkType: String) = Result.success(false)

    override suspend fun getWallets(
        sdkType: String,
        page: Int,
        search: String?,
        excludeIds: List<String>?,
        includeWallets: List<String>?
    ) = Result.success(
        (if (includeWallets != null) wallets.filter { wallet -> includeWallets.contains(wallet.id) }
        else if (excludeIds != null) wallets.filter { wallet -> excludeIds.contains(wallet.id).not() }
        else wallets).let { wallets ->
            WalletListing(
                page = 0,
                totalCount = wallets.size,
                wallets = wallets,
            )
        }
    )
}

private val wallets = listOf(
    Wallet(
        id = "CrossWallet",
        name = "CROSSx",
        homePage = "https://to.nexus",
        imageUrl = "https://contents.crosstoken.io/img/CROSSx_AppIcon.png",
        order = "1",
        mobileLink = "crossx://",
        playStore = "https://play.google.com/store/apps/details?id=com.nexus.crosswallet",
        webAppLink = null,
        linkMode = "",
        true
    )
).plus(
    if (BuildConfig.DEBUG) listOf(
        Wallet(
            id = "CrossWalletDebug",
            name = "CROSSx (debug)",
            homePage = "https://to.nexus",
            imageUrl = "https://contents.crosstoken.io/img/CROSSx_AppIcon.png",
            order = "2",
            mobileLink = "crossx://",
            playStore = "https://play.google.com/store/apps/details?id=com.nexus.crosswallet.debug",
            webAppLink = null,
            linkMode = "",
            true
        ),
        Wallet(
            id = "CrossWalletDev",
            name = "CROSSx (dev)",
            homePage = "https://to.nexus",
            imageUrl = "https://contents.crosstoken.io/img/CROSSx_AppIcon.png",
            order = "3",
            mobileLink = "crossx://",
            playStore = "https://play.google.com/store/apps/details?id=com.nexus.crosswallet.dev",
            webAppLink = null,
            linkMode = "",
            true
        )
    )
    else emptyList()
)