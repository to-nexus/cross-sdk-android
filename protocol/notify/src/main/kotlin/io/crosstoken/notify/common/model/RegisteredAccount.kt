package io.crosstoken.notify.common.model

import io.crosstoken.android.internal.common.model.AccountId
import io.crosstoken.foundation.common.model.PublicKey
import io.crosstoken.foundation.common.model.Topic

data class RegisteredAccount(
    val accountId: AccountId,
    val publicIdentityKey: PublicKey,
    val allApps: Boolean,
    val appDomain: String?,
    val notifyServerWatchTopic: Topic?,
    val notifyServerAuthenticationKey: PublicKey?,
)