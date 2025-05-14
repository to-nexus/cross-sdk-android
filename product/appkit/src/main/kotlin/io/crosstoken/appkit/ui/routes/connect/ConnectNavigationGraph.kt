package io.crosstoken.appkit.ui.routes.connect

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import io.crosstoken.appkit.ui.navigation.ConsumeNavigationEventsEffect
import io.crosstoken.appkit.ui.navigation.Route
import io.crosstoken.appkit.ui.navigation.connection.redirectRoute
import io.crosstoken.appkit.ui.routes.account.siwe_fallback.SIWEFallbackRoute
import io.crosstoken.appkit.ui.routes.common.WhatIsNetworkRoute
import io.crosstoken.appkit.ui.routes.connect.all_wallets.AllWalletsRoute
import io.crosstoken.appkit.ui.routes.connect.choose_network.ChooseNetworkRoute
import io.crosstoken.appkit.ui.routes.connect.connect_wallet.ConnectWalletRoute
import io.crosstoken.appkit.ui.routes.connect.get_wallet.GetAWalletRoute
import io.crosstoken.appkit.ui.routes.connect.scan_code.ScanQRCodeRoute
import io.crosstoken.appkit.ui.routes.connect.what_is_wallet.WhatIsWallet
import io.crosstoken.appkit.ui.utils.AnimatedNavGraph
import io.crosstoken.appkit.ui.utils.animatedComposable

@Composable
internal fun ConnectionNavGraph(
    navController: NavHostController,
    closeModal: () -> Unit,
    shouldOpenChooseNetwork: Boolean
) {
    val connectViewModel = viewModel<ConnectViewModel>()
    val startDestination = if (shouldOpenChooseNetwork) {
        Route.CHOOSE_NETWORK.path
    } else {
        Route.CONNECT_YOUR_WALLET.path
    }

    ConsumeNavigationEventsEffect(
        navController = navController,
        navigator = connectViewModel,
        closeModal = closeModal
    )

    AnimatedNavGraph(
        navController = navController,
        startDestination = startDestination
    ) {
        animatedComposable(route = Route.SIWE_FALLBACK.path) {
            SIWEFallbackRoute(connectViewModel = connectViewModel)
        }
        animatedComposable(route = Route.CONNECT_YOUR_WALLET.path) {
            ConnectWalletRoute(connectViewModel = connectViewModel)
        }
        animatedComposable(route = Route.QR_CODE.path) {
            ScanQRCodeRoute(connectViewModel = connectViewModel)
        }
        animatedComposable(route = Route.WHAT_IS_WALLET.path) {
            WhatIsWallet(navController = navController)
        }
        animatedComposable(Route.GET_A_WALLET.path) {
            GetAWalletRoute(wallets = connectViewModel.getNotInstalledWallets())
        }
        animatedComposable(Route.ALL_WALLETS.path) {
            AllWalletsRoute(connectViewModel = connectViewModel)
        }
        redirectRoute(connectViewModel)
        animatedComposable(Route.CHOOSE_NETWORK.path) {
            ChooseNetworkRoute(connectViewModel = connectViewModel)
        }
        animatedComposable(Route.WHAT_IS_NETWORK.path) {
            WhatIsNetworkRoute()
        }
    }
}
