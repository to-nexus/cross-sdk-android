@file:JvmSynthetic

package io.crosstoken.notify.di

import io.crosstoken.android.internal.common.di.AndroidCommonDITags
import io.crosstoken.notify.engine.responses.OnDeleteResponseUseCase
import io.crosstoken.notify.engine.responses.OnGetNotificationsResponseUseCase
import io.crosstoken.notify.engine.responses.OnSubscribeResponseUseCase
import io.crosstoken.notify.engine.responses.OnUpdateResponseUseCase
import io.crosstoken.notify.engine.responses.OnWatchSubscriptionsResponseUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun responseModule() = module {

    single {
        OnSubscribeResponseUseCase(
            setActiveSubscriptionsUseCase = get(),
            findRequestedSubscriptionUseCase = get(),
            subscriptionRepository = get(),
            jsonRpcInteractor = get(),
            logger = get()
        )
    }

    single {
        OnUpdateResponseUseCase(
            setActiveSubscriptionsUseCase = get(),
            findRequestedSubscriptionUseCase = get(),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }

    single {
        OnDeleteResponseUseCase(
            setActiveSubscriptionsUseCase = get(),
            jsonRpcInteractor = get(),
            notificationsRepository = get(),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }

    single {
        OnWatchSubscriptionsResponseUseCase(
            setActiveSubscriptionsUseCase = get(),
            extractPublicKeysFromDidJsonUseCase = get(),
            watchSubscriptionsForEveryRegisteredAccountUseCase = get(),
            accountsRepository = get(),
            notifyServerUrl = get(),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }

    single {
        OnGetNotificationsResponseUseCase(
            logger = get(named(AndroidCommonDITags.LOGGER)),
            metadataStorageRepository = get(),
            notificationsRepository = get(),
            subscriptionRepository = get()
        )
    }
}