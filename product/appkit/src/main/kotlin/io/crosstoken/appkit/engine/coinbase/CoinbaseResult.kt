package io.crosstoken.appkit.engine.coinbase

sealed class CoinbaseResult {
    data class Result(val value: String) : CoinbaseResult()

    data class Error(val code: Long, val message: String) : CoinbaseResult()
}