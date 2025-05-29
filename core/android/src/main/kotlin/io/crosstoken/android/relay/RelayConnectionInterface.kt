package io.crosstoken.android.relay

import io.crosstoken.android.Core
import io.crosstoken.foundation.network.RelayInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface RelayConnectionInterface : RelayInterface {
    val wssConnectionState: StateFlow<WSSConnectionState>
    val isNetworkAvailable: StateFlow<Boolean?>
    val onResubscribe: Flow<Any?>

    fun connect(onError: (Core.Model.Error) -> Unit)
    fun disconnect(onError: (Core.Model.Error) -> Unit)

    fun restart(onError: (Core.Model.Error) -> Unit)
}