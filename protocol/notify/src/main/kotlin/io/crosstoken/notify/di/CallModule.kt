@file:JvmSynthetic

package io.crosstoken.notify.di

import io.crosstoken.android.internal.common.di.AndroidCommonDITags
import io.crosstoken.android.internal.common.model.Tags
import io.crosstoken.android.push.notifications.DecryptMessageUseCaseInterface
import io.crosstoken.notify.engine.calls.DecryptNotifyMessageUseCase
import io.crosstoken.notify.engine.calls.DeleteSubscriptionUseCase
import io.crosstoken.notify.engine.calls.DeleteSubscriptionUseCaseInterface
import io.crosstoken.notify.engine.calls.GetActiveSubscriptionsUseCase
import io.crosstoken.notify.engine.calls.GetActiveSubscriptionsUseCaseInterface
import io.crosstoken.notify.engine.calls.GetNotificationHistoryUseCase
import io.crosstoken.notify.engine.calls.GetNotificationHistoryUseCaseInterface
import io.crosstoken.notify.engine.calls.GetNotificationTypesUseCase
import io.crosstoken.notify.engine.calls.GetNotificationTypesUseCaseInterface
import io.crosstoken.notify.engine.calls.IsRegisteredUseCase
import io.crosstoken.notify.engine.calls.IsRegisteredUseCaseInterface
import io.crosstoken.notify.engine.calls.PrepareRegistrationUseCase
import io.crosstoken.notify.engine.calls.PrepareRegistrationUseCaseInterface
import io.crosstoken.notify.engine.calls.RegisterUseCase
import io.crosstoken.notify.engine.calls.RegisterUseCaseInterface
import io.crosstoken.notify.engine.calls.SubscribeToDappUseCase
import io.crosstoken.notify.engine.calls.SubscribeToDappUseCaseInterface
import io.crosstoken.notify.engine.calls.UnregisterUseCase
import io.crosstoken.notify.engine.calls.UnregisterUseCaseInterface
import io.crosstoken.notify.engine.calls.UpdateSubscriptionUseCase
import io.crosstoken.notify.engine.calls.UpdateSubscriptionUseCaseInterface
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun callModule() = module {

    single<SubscribeToDappUseCaseInterface> {
        SubscribeToDappUseCase(
            jsonRpcInteractor = get(),
            crypto = get(),
            extractMetadataFromConfigUseCase = get(),
            metadataStorageRepository = get(),
            fetchDidJwtInteractor = get(),
            extractPublicKeysFromDidJson = get(),
            onSubscribeResponseUseCase = get(),
            subscriptionRepository = get(),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }

    single<UpdateSubscriptionUseCaseInterface> {
        UpdateSubscriptionUseCase(
            jsonRpcInteractor = get(),
            subscriptionRepository = get(),
            metadataStorageRepository = get(),
            fetchDidJwtInteractor = get(),
            onUpdateResponseUseCase = get()
        )
    }

    single<DeleteSubscriptionUseCaseInterface> {
        DeleteSubscriptionUseCase(
            jsonRpcInteractor = get(),
            metadataStorageRepository = get(),
            subscriptionRepository = get(),
            fetchDidJwtInteractor = get(),
            onDeleteResponseUseCase = get()
        )
    }

    single<DecryptMessageUseCaseInterface>(named(AndroidCommonDITags.DECRYPT_NOTIFY_MESSAGE)) {
        val useCase = DecryptNotifyMessageUseCase(
            codec = get(),
            serializer = get(),
            jsonRpcHistory = get(),
            notificationsRepository = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            metadataStorageRepository = get()
        )

        get<MutableMap<String, DecryptMessageUseCaseInterface>>(named(AndroidCommonDITags.DECRYPT_USE_CASES))[Tags.NOTIFY_MESSAGE.id.toString()] = useCase
        useCase
    }

    single<RegisterUseCaseInterface> {
        RegisterUseCase(
            registeredAccountsRepository = get(),
            identitiesInteractor = get(),
            watchSubscriptionsUseCase = get(),
            keyManagementRepository = get(),
            projectId = get()
        )
    }

    single<IsRegisteredUseCaseInterface> {
        IsRegisteredUseCase(
            registeredAccountsRepository = get(),
            identitiesInteractor = get(),
            identityServerUrl = get(named(AndroidCommonDITags.KEYSERVER_URL))
        )
    }

    single<PrepareRegistrationUseCaseInterface> {
        PrepareRegistrationUseCase(
            identitiesInteractor = get(),
            identityServerUrl = get(named(AndroidCommonDITags.KEYSERVER_URL)),
            keyManagementRepository = get(),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }

    single<UnregisterUseCaseInterface> {
        UnregisterUseCase(
            registeredAccountsRepository = get(),
            stopWatchingSubscriptionsUseCase = get(),
            identitiesInteractor = get(),
            keyserverUrl = get(named(AndroidCommonDITags.KEYSERVER_URL)),
            subscriptionRepository = get(),
            notificationsRepository = get(),
            jsonRpcInteractor = get()
        )
    }

    single<GetNotificationTypesUseCaseInterface> {
        GetNotificationTypesUseCase(
            getNotifyConfigUseCase = get()
        )
    }

    single<GetActiveSubscriptionsUseCaseInterface> {
        GetActiveSubscriptionsUseCase(
            subscriptionRepository = get(),
            metadataStorageRepository = get()
        )
    }

    single<GetNotificationHistoryUseCaseInterface> {
        GetNotificationHistoryUseCase(
            jsonRpcInteractor = get(),
            subscriptionRepository = get(),
            metadataStorageRepository = get(),
            notificationsRepository = get(),
            fetchDidJwtInteractor = get(),
            onGetNotificationsResponseUseCase = get(),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }
}