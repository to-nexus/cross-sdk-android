package io.crosstoken.appkit.ui.routes.connect

import com.android.resources.NightMode
import io.crosstoken.android.internal.common.modal.data.model.Wallet
import io.crosstoken.modal.ui.model.UiState
import io.crosstoken.appkit.ui.navigation.Route
import io.crosstoken.appkit.ui.previews.testWallets
import io.crosstoken.appkit.ui.routes.connect.connect_wallet.ConnectWalletRoute
import io.crosstoken.appkit.utils.MainDispatcherRule
import io.crosstoken.appkit.utils.ScreenShotTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

@Ignore("This test is not working on CI for Sonar only")
internal class ConnectYourWalletRouteTest : ScreenShotTest("connect/${Route.CONNECT_YOUR_WALLET.path}") {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val viewModel: ConnectViewModel = mockk()
    private val uiState: StateFlow<UiState<List<Wallet>>> = mockk()

    @Before
    fun setup() {
        every { viewModel.uiState } returns uiState
    }

    @Test
    fun `test ConnectYourWallet with Loading UiState in LightMode`() = runRouteScreenShotTest(
        title = Route.CONNECT_YOUR_WALLET.title
    ) {
        every { uiState.value } returns UiState.Loading()
        ConnectWalletRoute(connectViewModel = viewModel)
    }

    @Test
    fun `test ConnectYourWallet with Loading UiState in DarkMode`() = runRouteScreenShotTest(
        title = Route.CONNECT_YOUR_WALLET.title,
        nightMode = NightMode.NIGHT
    ) {
        every { uiState.value } returns UiState.Loading()
        ConnectWalletRoute(connectViewModel = viewModel)
    }

    @Test
    fun `test ConnectYourWallet with Error UiState in LightMode`() = runRouteScreenShotTest(
        title = Route.CONNECT_YOUR_WALLET.title
    ) {
        every { uiState.value } returns UiState.Error(Throwable("Connection error"))
        ConnectWalletRoute(connectViewModel = viewModel)
    }

    @Test
    fun `test ConnectYourWallet with Error UiState in DarkMode`() = runRouteScreenShotTest(
        title = Route.CONNECT_YOUR_WALLET.title,
        nightMode = NightMode.NIGHT
    ) {
        every { uiState.value } returns UiState.Error(Throwable("Connection error"))
        ConnectWalletRoute(connectViewModel = viewModel)
    }

    @Test
    fun `test ConnectYourWallet with Success UiState in LightMode`() = runRouteScreenShotTest(
        title = Route.CONNECT_YOUR_WALLET.title
    ) {
        every { uiState.value } returns UiState.Success(testWallets)
        every { viewModel.getWalletsTotalCount() } returns testWallets.size
        ConnectWalletRoute(connectViewModel = viewModel)
    }

    @Test
    fun `test ConnectYourWallet with Success UiState in DarkMode`() = runRouteScreenShotTest(
        title = Route.CONNECT_YOUR_WALLET.title,
        nightMode = NightMode.NIGHT
    ) {
        every { uiState.value } returns UiState.Success(testWallets)
        every { viewModel.getWalletsTotalCount() } returns testWallets.size
        ConnectWalletRoute(connectViewModel = viewModel)
    }
}