package io.crosstoken.android

import android.app.Application
//import io.crosstoken.android.internal.common.explorer.ExplorerInterface
import io.crosstoken.android.pairing.client.PairingInterface
import io.crosstoken.android.pairing.handler.PairingControllerInterface
import io.crosstoken.android.push.PushInterface
import io.crosstoken.android.relay.ConnectionType
import io.crosstoken.android.relay.NetworkClientTimeout
import io.crosstoken.android.relay.RelayConnectionInterface
import io.crosstoken.android.verify.client.VerifyInterface

interface CoreInterface {
    val Pairing: PairingInterface
    val PairingController: PairingControllerInterface
    val Relay: RelayConnectionInterface
    val Push: PushInterface
    val Verify: VerifyInterface
    //val Explorer: ExplorerInterface

    interface Delegate : PairingInterface.Delegate

    fun setDelegate(delegate: Delegate)

    fun initialize(
        metaData: Core.Model.AppMetaData,
        relayServerUrl: String,
        connectionType: ConnectionType = ConnectionType.AUTOMATIC,
        application: Application,
        relay: RelayConnectionInterface? = null,
        networkClientTimeout: NetworkClientTimeout? = null,
        onError: (Core.Model.Error) -> Unit,
    )

    fun initialize(
        application: Application,
        projectId: String,
        metaData: Core.Model.AppMetaData,
        connectionType: ConnectionType = ConnectionType.AUTOMATIC,
        relay: RelayConnectionInterface? = null,
        networkClientTimeout: NetworkClientTimeout? = null,
        onError: (Core.Model.Error) -> Unit,
    )
}