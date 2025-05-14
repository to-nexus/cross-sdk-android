@file:JvmSynthetic

package io.crosstoken.android.relay

sealed class WSSConnectionState {
	data object Connected : WSSConnectionState()

	sealed class Disconnected : WSSConnectionState() {
		data class ConnectionFailed(val throwable: Throwable) : Disconnected()
		data class ConnectionClosed(val message: String? = null) : Disconnected()
	}
}