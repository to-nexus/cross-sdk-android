package io.crosstoken.android

import android.app.Application
import android.content.SharedPreferences
import io.crosstoken.android.di.coreStorageModule
import io.crosstoken.android.internal.common.di.AndroidCommonDITags
import io.crosstoken.android.internal.common.di.KEY_CLIENT_ID
import io.crosstoken.android.internal.common.di.coreAndroidNetworkModule
import io.crosstoken.android.internal.common.di.coreCommonModule
import io.crosstoken.android.internal.common.di.coreCryptoModule
import io.crosstoken.android.internal.common.di.coreJsonRpcModule
import io.crosstoken.android.internal.common.di.corePairingModule
import io.crosstoken.android.internal.common.di.explorerModule
import io.crosstoken.android.internal.common.di.keyServerModule
import io.crosstoken.android.internal.common.di.pulseModule
import io.crosstoken.android.internal.common.di.pushModule
import io.crosstoken.android.internal.common.di.appKitModule
import io.crosstoken.android.internal.common.explorer.ExplorerInterface
import io.crosstoken.android.internal.common.explorer.ExplorerProtocol
import io.crosstoken.android.internal.common.model.AppMetaData
import io.crosstoken.android.internal.common.model.ProjectId
import io.crosstoken.android.internal.common.model.Redirect
import io.crosstoken.android.internal.common.model.TelemetryEnabled
import io.crosstoken.android.internal.common.wcKoinApp
import io.crosstoken.android.pairing.client.PairingInterface
import io.crosstoken.android.pairing.client.PairingProtocol
import io.crosstoken.android.pairing.handler.PairingController
import io.crosstoken.android.pairing.handler.PairingControllerInterface
import io.crosstoken.android.push.PushInterface
import io.crosstoken.android.push.client.PushClient
import io.crosstoken.android.relay.ConnectionType
import io.crosstoken.android.relay.NetworkClientTimeout
import io.crosstoken.android.relay.RelayClient
import io.crosstoken.android.relay.RelayConnectionInterface
import io.crosstoken.android.utils.isValidRelayServerUrl
import io.crosstoken.android.utils.plantTimber
import io.crosstoken.android.utils.projectId
import io.crosstoken.android.verify.client.VerifyClient
import io.crosstoken.android.verify.client.VerifyInterface
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module

class CoreProtocol(private val koinApp: KoinApplication = wcKoinApp) : CoreInterface {
    override val Pairing: PairingInterface = PairingProtocol(koinApp)
    override val PairingController: PairingControllerInterface = PairingController(koinApp)
    override var Relay = RelayClient(koinApp)

    @Deprecated(message = "Replaced with Push")
    override val Echo: PushInterface = PushClient
    override val Push: PushInterface = PushClient
    override val Verify: VerifyInterface = VerifyClient(koinApp)
    override val Explorer: ExplorerInterface = ExplorerProtocol(koinApp)

    init {
        plantTimber()
    }

    override fun setDelegate(delegate: CoreInterface.Delegate) {
        Pairing.setDelegate(delegate)
    }

    companion object {
        val instance = CoreProtocol()
    }

    override fun initialize(
        metaData: Core.Model.AppMetaData,
        relayServerUrl: String,
        connectionType: ConnectionType,
        application: Application,
        relay: RelayConnectionInterface?,
        keyServerUrl: String?,
        networkClientTimeout: NetworkClientTimeout?,
        telemetryEnabled: Boolean,
        onError: (Core.Model.Error) -> Unit
    ) {
        try {
            require(relayServerUrl.isValidRelayServerUrl()) { "Check the schema and projectId parameter of the Server Url" }

            setup(
                application = application,
                serverUrl = relayServerUrl,
                projectId = relayServerUrl.projectId(),
                telemetryEnabled = telemetryEnabled,
                connectionType = connectionType,
                networkClientTimeout = networkClientTimeout,
                relay = relay,
                onError = onError,
                metaData = metaData,
                keyServerUrl = keyServerUrl
            )
        } catch (e: Exception) {
            onError(Core.Model.Error(e))
        }
    }

    override fun initialize(
        application: Application,
        projectId: String,
        metaData: Core.Model.AppMetaData,
        connectionType: ConnectionType,
        relay: RelayConnectionInterface?,
        keyServerUrl: String?,
        networkClientTimeout: NetworkClientTimeout?,
        telemetryEnabled: Boolean,
        onError: (Core.Model.Error) -> Unit
    ) {
        try {
            require(projectId.isNotEmpty()) { "Project Id cannot be empty" }

            setup(
                application = application,
                projectId = projectId,
                telemetryEnabled = telemetryEnabled,
                connectionType = connectionType,
                networkClientTimeout = networkClientTimeout,
                relay = relay,
                onError = onError,
                metaData = metaData,
                keyServerUrl = keyServerUrl
            )
        } catch (e: Exception) {
            onError(Core.Model.Error(e))
        }
    }

    private fun CoreProtocol.setup(
        application: Application,
        serverUrl: String? = null,
        projectId: String,
        telemetryEnabled: Boolean,
        connectionType: ConnectionType,
        networkClientTimeout: NetworkClientTimeout?,
        relay: RelayConnectionInterface?,
        onError: (Core.Model.Error) -> Unit,
        metaData: Core.Model.AppMetaData,
        keyServerUrl: String?
    ) {
        val packageName: String = application.packageName
        val relayServerUrl = if (serverUrl.isNullOrEmpty()) "wss://cross-relay.crosstoken.io?projectId=$projectId" else serverUrl

        with(koinApp) {
            androidContext(application)
            modules(
                module { single(named(AndroidCommonDITags.PACKAGE_NAME)) { packageName } },
                module { single { ProjectId(projectId) } },
                module { single(named(AndroidCommonDITags.TELEMETRY_ENABLED)) { TelemetryEnabled(telemetryEnabled) } },
                coreAndroidNetworkModule(relayServerUrl, connectionType, BuildConfig.SDK_VERSION, networkClientTimeout, packageName),
                coreCommonModule(),
                coreCryptoModule(),
            )

            if (relay == null) {
                Relay.initialize(connectionType) { error -> onError(Core.Model.Error(error)) }
            }

            modules(
                coreStorageModule(packageName = packageName),
                module { single(named(AndroidCommonDITags.CLIENT_ID)) { requireNotNull(get<SharedPreferences>().getString(KEY_CLIENT_ID, null)) } },
                pushModule(),
                module { single { relay ?: Relay } },
                module {
                    single {
                        with(metaData) {
                            AppMetaData(
                                name = name,
                                description = description,
                                url = url,
                                icons = icons,
                                redirect = Redirect(native = redirect, universal = appLink, linkMode = linkMode)
                            )
                        }
                    }
                },
                module { single { Echo } },
                module { single { Push } },
                module { single { Verify } },
                coreJsonRpcModule(),
                corePairingModule(Pairing, PairingController),
                keyServerModule(keyServerUrl),
                explorerModule(),
                appKitModule(),
                pulseModule(packageName)
            )
        }

        Pairing.initialize()
        PairingController.initialize()
        Verify.initialize()
    }
}