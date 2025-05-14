package io.crosstoken.sign.di

import io.crosstoken.android.di.coreStorageModule
import io.crosstoken.android.internal.common.di.coreAndroidNetworkModule
import io.crosstoken.android.internal.common.di.coreCryptoModule
import io.crosstoken.android.internal.common.di.coreJsonRpcModule
import io.crosstoken.android.internal.common.di.corePairingModule
import io.crosstoken.android.pairing.client.PairingInterface
import io.crosstoken.android.pairing.handler.PairingControllerInterface
import io.crosstoken.android.relay.ConnectionType
import io.crosstoken.android.relay.RelayConnectionInterface
import org.koin.dsl.module

private const val SHARED_PREFS_FILE = "wc_key_store"
private const val KEY_STORE_ALIAS = "wc_keystore_key"

// When called more that once, different `storagePrefix` must be defined.
@JvmSynthetic
internal fun overrideModule(
    relay: RelayConnectionInterface,
    pairing: PairingInterface,
    pairingController: PairingControllerInterface,
    storagePrefix: String,
    relayUrl: String,
    connectionType: ConnectionType,
    bundleId: String
) = module {
    val sharedPrefsFile = storagePrefix + SHARED_PREFS_FILE
    val keyStoreAlias = storagePrefix + KEY_STORE_ALIAS

    single { relay }

    includes(
        coreStorageModule(storagePrefix, bundleId),
        corePairingModule(pairing, pairingController),
        coreCryptoModule(sharedPrefsFile, keyStoreAlias),
        coreAndroidNetworkModule(relayUrl, connectionType, "test_version", packageName = bundleId),
        coreJsonRpcModule()
    )
}