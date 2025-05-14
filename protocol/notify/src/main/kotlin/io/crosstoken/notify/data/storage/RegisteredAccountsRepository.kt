@file:JvmSynthetic

package io.crosstoken.notify.data.storage

import io.crosstoken.android.internal.common.model.AccountId
import io.crosstoken.foundation.common.model.PublicKey
import io.crosstoken.foundation.common.model.Topic
import io.crosstoken.notify.common.model.RegisteredAccount
import io.crosstoken.notify.common.storage.data.dao.RegisteredAccountsQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class RegisteredAccountsRepository(private val registeredAccounts: RegisteredAccountsQueries) {

    suspend fun insertOrIgnoreAccount(accountId: AccountId, publicIdentityKey: PublicKey) = withContext(Dispatchers.IO) {
        registeredAccounts.insertOrIgnoreAccount(accountId.value, publicIdentityKey.keyAsHex, true, null)
    }

    suspend fun updateNotifyServerData(accountId: AccountId, notifyServerWatchTopic: Topic, notifyServerAuthenticationKey: PublicKey) = withContext(Dispatchers.IO) {
        registeredAccounts.updateNotifyServerData(
            accountId = accountId.value, notifyServerWatchTopic = notifyServerWatchTopic.value, notifyServerAuthenticationKey = notifyServerAuthenticationKey.keyAsHex
        )
    }

    suspend fun getAccountByAccountId(accountId: String): RegisteredAccount = withContext(Dispatchers.IO) {
        registeredAccounts.getAccountByAccountId(accountId, ::toRegisterAccount).executeAsOne()
    }

    suspend fun getAccountByIdentityKey(identityPublicKey: String): RegisteredAccount = withContext(Dispatchers.IO) {
        registeredAccounts.getAccountByIdentityKey(identityPublicKey, ::toRegisterAccount).executeAsOne()
    }

    suspend fun getAllAccounts(): List<RegisteredAccount> = withContext(Dispatchers.IO) {
        registeredAccounts.getAllAccounts(::toRegisterAccount).executeAsList()
    }

    suspend fun deleteAccountByAccountId(accountId: String) = withContext(Dispatchers.IO) {
        registeredAccounts.deleteAccountByAccountId(accountId)
    }

    private fun toRegisterAccount(
        accountId: String, publicIdentityKey: String, allApps: Boolean, appDomain: String?, notifyServerWatchTopic: String?, notifyServerAuthenticationKey: String?,
    ): RegisteredAccount =
        RegisteredAccount(AccountId(accountId), PublicKey(publicIdentityKey), allApps, appDomain, notifyServerWatchTopic?.let { Topic(it) }, notifyServerAuthenticationKey?.let { PublicKey(it) })
}