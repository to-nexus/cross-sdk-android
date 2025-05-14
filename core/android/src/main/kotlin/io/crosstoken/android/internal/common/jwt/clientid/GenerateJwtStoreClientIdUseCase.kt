@file:JvmSynthetic

package io.crosstoken.android.internal.common.jwt.clientid

import android.content.SharedPreferences
import androidx.core.content.edit
import io.crosstoken.android.internal.common.di.KEY_CLIENT_ID
import io.crosstoken.android.utils.strippedUrl
import io.crosstoken.foundation.crypto.data.repository.ClientIdJwtRepository

internal class GenerateJwtStoreClientIdUseCase(private val clientIdJwtRepository: ClientIdJwtRepository, private val sharedPreferences: SharedPreferences) {

    operator fun invoke(relayUrl: String): String =
        clientIdJwtRepository.generateJWT(relayUrl.strippedUrl()) { clientId ->
            sharedPreferences.edit {
                putString(KEY_CLIENT_ID, clientId)
            }
        }
}