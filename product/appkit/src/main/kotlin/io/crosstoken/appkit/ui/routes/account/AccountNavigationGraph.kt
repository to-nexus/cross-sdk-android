package io.crosstoken.appkit.ui.routes.account

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import io.crosstoken.appkit.ui.navigation.ConsumeNavigationEventsEffect
import io.crosstoken.appkit.ui.navigation.Route
import io.crosstoken.appkit.ui.navigation.account.chainSwitchRoute
import io.crosstoken.appkit.ui.routes.account.account.AccountRoute
import io.crosstoken.appkit.ui.routes.account.change_network.ChangeNetworkRoute
import io.crosstoken.appkit.ui.routes.account.what_is_network.WhatIsNetworkRoute
import io.crosstoken.appkit.ui.utils.AnimatedNavGraph
import io.crosstoken.appkit.ui.utils.animatedComposable

@Composable
internal fun AccountNavGraph(
    navController: NavHostController,
    closeModal: () -> Unit,
    shouldOpenChangeNetwork: Boolean
) {
    val startDestination = if (shouldOpenChangeNetwork) Route.CHANGE_NETWORK.path else Route.ACCOUNT.path
    val accountViewModel = viewModel<AccountViewModel>()

    ConsumeNavigationEventsEffect(
        navController = navController,
        navigator = accountViewModel,
        closeModal = closeModal
    )

    AnimatedNavGraph(
        navController = navController,
        startDestination = startDestination
    ) {
        animatedComposable(route = Route.ACCOUNT.path) {
            AccountRoute(
                accountViewModel = accountViewModel,
                navController = navController
            )
        }
        animatedComposable(route = Route.CHANGE_NETWORK.path) {
            ChangeNetworkRoute(accountViewModel = accountViewModel)
        }
        animatedComposable(route = Route.WHAT_IS_WALLET.path) {
            WhatIsNetworkRoute()
        }
        chainSwitchRoute(accountViewModel = accountViewModel)
    }
}
