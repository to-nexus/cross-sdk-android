package io.crosstoken.appkit.ui.routes.account

import com.android.resources.NightMode
import com.android.resources.ScreenOrientation
import io.crosstoken.android.internal.common.wcKoinApp
import io.crosstoken.modal.ui.model.UiState
import io.crosstoken.appkit.client.Modal
import io.crosstoken.appkit.client.AppKit
import io.crosstoken.appkit.domain.model.AccountData
import io.crosstoken.appkit.domain.usecase.GetSelectedChainUseCase
import io.crosstoken.appkit.presets.AppKitChainsPresets
import io.crosstoken.appkit.ui.navigation.Route
import io.crosstoken.appkit.ui.routes.account.change_network.ChangeNetworkRoute
import io.crosstoken.appkit.utils.ScreenShotTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.koin.dsl.module

@Ignore("This test is not working on CI for Sonar only")
internal class ChangeNetworkRouteTest: ScreenShotTest("account/${Route.CHANGE_NETWORK.path}")  {

    private val viewModel: AccountViewModel = mockk()
    private val selectedChain: StateFlow<Modal.Model.Chain> = mockk()
    private val uiState: StateFlow<UiState<AccountData>> = mockk()
    private val getSelectedChainUseCase: GetSelectedChainUseCase = mockk()

    @Before
    fun setup() {
        AppKit.chains = AppKitChainsPresets.ethChains.values.toList()
        every { viewModel.accountState } returns uiState
        every { uiState.value } returns UiState.Success(AccountData("0x2765d421FB91182490D602E671a", AppKitChainsPresets.ethChains.values.toList()))
        every { viewModel.selectedChain } returns selectedChain
        every { selectedChain.value } returns AppKitChainsPresets.ethChains["1"]!!
        every { getSelectedChainUseCase() } returns "1"
        every { viewModel.getSelectedChainOrFirst() } returns AppKitChainsPresets.ethChains.getOrElse("1") { throw IllegalStateException("Chain not found") }
        wcKoinApp.koin.loadModules(modules = listOf(module { single { getSelectedChainUseCase } }))

    }

    @Test
    fun `test ChangeNetworkRoute in LightMode`() = runRouteScreenShotTest(
        title = Route.CHANGE_NETWORK.title
    ) {
       ChangeNetworkRoute(accountViewModel = viewModel)
    }

    @Test
    fun `test ChangeNetworkRoute in DarkMode`() = runRouteScreenShotTest(
        title = Route.CHANGE_NETWORK.title,
        nightMode = NightMode.NIGHT
    ) {
        ChangeNetworkRoute(accountViewModel = viewModel)
    }

    @Test
    fun `test ChangeNetworkRoute in Landscape`() = runRouteScreenShotTest(
        title = Route.CHANGE_NETWORK.title,
        orientation = ScreenOrientation.LANDSCAPE
    ) {
        ChangeNetworkRoute(accountViewModel = viewModel)
    }
}