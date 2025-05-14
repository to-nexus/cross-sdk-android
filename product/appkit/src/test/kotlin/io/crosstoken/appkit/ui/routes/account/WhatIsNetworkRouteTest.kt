package io.crosstoken.appkit.ui.routes.account

import com.android.resources.NightMode
import com.android.resources.ScreenOrientation
import io.crosstoken.appkit.ui.navigation.Route
import io.crosstoken.appkit.ui.routes.account.what_is_network.WhatIsNetworkRoute
import io.crosstoken.appkit.utils.MainDispatcherRule
import io.crosstoken.appkit.utils.ScreenShotTest
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

@Ignore("This test is not working on CI for Sonar only")
internal class WhatIsNetworkRouteTest : ScreenShotTest("account/${Route.WHAT_IS_NETWORK.path}") {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test WhatIsWallet in LightMode`() = runRouteScreenShotTest(
        title = Route.WHAT_IS_WALLET.title
    ) {
        WhatIsNetworkRoute()
    }

    @Test
    fun `test WhatIsWallet in DarkMode`() = runRouteScreenShotTest(
        title = Route.WHAT_IS_WALLET.title,
        nightMode = NightMode.NIGHT
    ) {
        WhatIsNetworkRoute()
    }

    @Test
    fun `test WhatIsWallet in Landscape`() = runRouteScreenShotTest(
        title = Route.WHAT_IS_WALLET.title,
        orientation = ScreenOrientation.LANDSCAPE
    ) {
        WhatIsNetworkRoute()
    }
}