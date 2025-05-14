package io.crosstoken.appkit.client.models

import io.crosstoken.android.internal.common.exception.WalletConnectException

class AppKitClientAlreadyInitializedException : WalletConnectException("AppKit already initialized")
class CoinbaseClientAlreadyInitializedException : WalletConnectException("Coinbase already initialized")