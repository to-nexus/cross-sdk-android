package io.crosstoken.android.push.notifications

import io.crosstoken.android.Core

interface DecryptMessageUseCaseInterface {
    suspend fun decryptNotification(topic: String, message: String, onSuccess: (Core.Model.Message) -> Unit, onFailure: (Throwable) -> Unit)
}