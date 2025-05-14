@file:JvmSynthetic

package io.crosstoken.foundation.di

import io.crosstoken.foundation.common.model.PrivateKey
import io.crosstoken.foundation.common.model.PublicKey
import io.crosstoken.foundation.crypto.data.repository.BaseClientIdJwtRepository
import io.crosstoken.foundation.crypto.data.repository.ClientIdJwtRepository
import org.koin.dsl.module

internal fun cryptoModule() = module {

    single<ClientIdJwtRepository> {
        object: BaseClientIdJwtRepository() {
            override fun setKeyPair(key: String, privateKey: PrivateKey, publicKey: PublicKey) {}
        }
    }
}
