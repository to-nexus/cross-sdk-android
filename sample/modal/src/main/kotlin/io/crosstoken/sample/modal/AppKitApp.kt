package io.crosstoken.sample.modal

import android.app.Application
//import com.google.firebase.appdistribution.FirebaseAppDistribution
//import com.google.firebase.crashlytics.ktx.crashlytics
//import com.google.firebase.ktx.Firebase
import io.crosstoken.android.Core
import io.crosstoken.android.CoreClient
import io.crosstoken.android.relay.ConnectionType
import io.crosstoken.sample.common.tag
import io.crosstoken.util.bytesToHex
import io.crosstoken.util.randomBytes
import io.crosstoken.appkit.client.Modal
import io.crosstoken.appkit.client.AppKit
import io.crosstoken.appkit.presets.AppKitChainsPresets
import io.crosstoken.appkit.utils.EthUtils
import timber.log.Timber
import io.crosstoken.sample.common.BuildConfig as CommonBuildConfig

class AppKitApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val appMetaData = Core.Model.AppMetaData(
            name = "Cross Sample Modal",
            description = "Cross modal implementation",
            url = "https://to.nexus",
            icons = listOf("https://contents.crosstoken.io/wallet/token/images/CROSSx.svg"),
            redirect = "cross-modal://request",
            linkMode = true,
            appLink = BuildConfig.LAB_APP_LINK
        )

        CoreClient.initialize(
            projectId = CommonBuildConfig.PROJECT_ID,
            crossProjectId = CommonBuildConfig.CROSS_PROJECT_ID,
            connectionType = ConnectionType.AUTOMATIC,
            application = this,
            metaData = appMetaData,
        ) {
            Timber.e(it.throwable)
        }

        AppKit.initialize(Modal.Params.Init(core = CoreClient)) { error ->
            Timber.e(tag(this), error.throwable.stackTraceToString())
            //Firebase.crashlytics.recordException(error.throwable)
        }

        AppKit.setChains(AppKitChainsPresets.ethChains.values.toList())

        val authParams = Modal.Model.AuthPayloadParams(
            chains = AppKitChainsPresets.ethChains.values.toList().map { it.id },
            domain = "sample.cross.modal",
            uri = "https://to.nexus",
            nonce = randomBytes(12).bytesToHex(),
            statement = "I accept the Terms of Service: https://to.nexus",
            methods = EthUtils.ethMethods
        )
        AppKit.setAuthRequestParams(authParams)

        //FirebaseAppDistribution.getInstance().updateIfNewReleaseAvailable()
    }
}
