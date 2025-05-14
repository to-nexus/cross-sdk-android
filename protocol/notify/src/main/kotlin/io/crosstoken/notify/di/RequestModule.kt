@file:JvmSynthetic

package io.crosstoken.notify.di

import io.crosstoken.android.internal.common.di.AndroidCommonDITags
import io.crosstoken.notify.engine.requests.OnMessageUseCase
import io.crosstoken.notify.engine.requests.OnSubscriptionsChangedUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun requestModule() = module {

    single {
        OnMessageUseCase(
            jsonRpcInteractor = get(),
            notificationsRepository = get(),
            subscriptionRepository = get(),
            fetchDidJwtInteractor = get(),
            metadataStorageRepository = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
        )
    }

    single {
        OnSubscriptionsChangedUseCase(
            setActiveSubscriptionsUseCase = get(),
            fetchDidJwtInteractor = get(),
            extractPublicKeysFromDidJsonUseCase = get(),
            jsonRpcInteractor = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            notifyServerUrl = get(),
            registeredAccountsRepository = get(),
            watchSubscriptionsForEveryRegisteredAccountUseCase = get(),
        )
    }
}