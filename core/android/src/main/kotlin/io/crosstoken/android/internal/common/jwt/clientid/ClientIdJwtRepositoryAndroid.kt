@file:JvmSynthetic

package io.crosstoken.android.internal.common.jwt.clientid

import io.crosstoken.android.internal.common.exception.CannotFindKeyPairException
import io.crosstoken.android.internal.common.storage.key_chain.KeyStore
import io.crosstoken.foundation.common.model.PrivateKey
import io.crosstoken.foundation.common.model.PublicKey
import io.crosstoken.foundation.crypto.data.repository.BaseClientIdJwtRepository

internal class ClientIdJwtRepositoryAndroid(private val keyChain: KeyStore) : BaseClientIdJwtRepository() {

    override fun setKeyPair(key: String, privateKey: PrivateKey, publicKey: PublicKey) {
        keyChain.setKeys(CLIENT_ID_KEYPAIR_TAG, privateKey, publicKey)
    }

    override fun getKeyPair(): Pair<String, String> {
        return if (doesKeyPairExist()) {
            val (privateKey, publicKey) = keyChain.getKeys(CLIENT_ID_KEYPAIR_TAG)
                ?: throw CannotFindKeyPairException("No key pair for given tag: $CLIENT_ID_KEYPAIR_TAG")
            publicKey to privateKey
        } else {
            generateAndStoreClientIdKeyPair()
        }
    }

    private fun doesKeyPairExist(): Boolean {
        return keyChain.checkKeys(CLIENT_ID_KEYPAIR_TAG)
    }
}