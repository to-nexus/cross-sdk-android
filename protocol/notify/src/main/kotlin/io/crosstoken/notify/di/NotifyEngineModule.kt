@file:JvmSynthetic

package io.crosstoken.notify.di

import io.crosstoken.android.internal.common.di.AndroidCommonDITags
import io.crosstoken.notify.common.NotifyServerUrl
import io.crosstoken.notify.engine.NotifyEngine
import io.crosstoken.notify.engine.calls.GetAllActiveSubscriptionsUseCase
import io.crosstoken.notify.engine.domain.ExtractMetadataFromConfigUseCase
import io.crosstoken.notify.engine.domain.ExtractPublicKeysFromDidJsonUseCase
import io.crosstoken.notify.engine.domain.FetchDidJwtInteractor
import io.crosstoken.notify.engine.domain.FindRequestedSubscriptionUseCase
import io.crosstoken.notify.engine.domain.GenerateAppropriateUriUseCase
import io.crosstoken.notify.engine.domain.GetSelfKeyForWatchSubscriptionUseCase
import io.crosstoken.notify.engine.domain.SetActiveSubscriptionsUseCase
import io.crosstoken.notify.engine.domain.StopWatchingSubscriptionsUseCase
import io.crosstoken.notify.engine.domain.WatchSubscriptionsForEveryRegisteredAccountUseCase
import io.crosstoken.notify.engine.domain.WatchSubscriptionsUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule() = module {

    single { NotifyServerUrl() }

    includes(
        callModule(),
        requestModule(),
        responseModule()
    )

    single {
        ExtractPublicKeysFromDidJsonUseCase(
            serializer = get(),
            generateAppropriateUri = get(),
        )
    }

    single {
        ExtractMetadataFromConfigUseCase(
            getNotifyConfigUseCase = get(),
        )
    }

    single {
        GenerateAppropriateUriUseCase()
    }


    single {
        FetchDidJwtInteractor(
            keyserverUrl = get(named(AndroidCommonDITags.KEYSERVER_URL)),
            identitiesInteractor = get()
        )
    }

    single {
        GetSelfKeyForWatchSubscriptionUseCase(
            keyManagementRepository = get(),
        )
    }

    single {
        WatchSubscriptionsUseCase(
            jsonRpcInteractor = get(),
            fetchDidJwtInteractor = get(),
            keyManagementRepository = get(),
            extractPublicKeysFromDidJsonUseCase = get(),
            notifyServerUrl = get(),
            registeredAccountsRepository = get(),
            getSelfKeyForWatchSubscriptionUseCase = get(),
        )
    }

    single {
        StopWatchingSubscriptionsUseCase(
            jsonRpcInteractor = get(),
            registeredAccountsRepository = get()
        )
    }

    single {
        WatchSubscriptionsForEveryRegisteredAccountUseCase(
            watchSubscriptionsUseCase = get(), registeredAccountsRepository = get(), logger = get()
        )
    }

    single {
        SetActiveSubscriptionsUseCase(
            subscriptionRepository = get(),
            extractMetadataFromConfigUseCase = get(),
            metadataRepository = get(),
            jsonRpcInteractor = get(),
            keyStore = get(),
        )
    }

    single {
        FindRequestedSubscriptionUseCase(
            metadataStorageRepository = get()
        )
    }

    single {
        GetAllActiveSubscriptionsUseCase(
            subscriptionRepository = get(),
            metadataStorageRepository = get()
        )
    }

    single {
        NotifyEngine(
            jsonRpcInteractor = get(),
            pairingHandler = get(),
            subscribeToDappUseCase = get(),
            updateUseCase = get(),
            deleteSubscriptionUseCase = get(),
            decryptMessageUseCase = get(named(AndroidCommonDITags.DECRYPT_NOTIFY_MESSAGE)),
            unregisterUseCase = get(),
            getNotificationTypesUseCase = get(),
            getActiveSubscriptionsUseCase = get(),
            getAllActiveSubscriptionsUseCase = get(),
            getNotificationHistoryUseCase = get(),
            onMessageUseCase = get(),
            onSubscribeResponseUseCase = get(),
            onUpdateResponseUseCase = get(),
            onDeleteResponseUseCase = get(),
            onWatchSubscriptionsResponseUseCase = get(),
            onGetNotificationsResponseUseCase = get(),
            watchSubscriptionsForEveryRegisteredAccountUseCase = get(),
            onSubscriptionsChangedUseCase = get(),
            isRegisteredUseCase = get(),
            prepareRegistrationUseCase = get(),
            registerUseCase = get(),
        )
    }
}

