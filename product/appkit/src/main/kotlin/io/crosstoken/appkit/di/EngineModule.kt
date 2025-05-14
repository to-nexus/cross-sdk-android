package io.crosstoken.appkit.di

import android.content.Context
import io.crosstoken.android.internal.common.di.AndroidCommonDITags
import io.crosstoken.appkit.domain.usecase.ConnectionEventRepository
import io.crosstoken.appkit.engine.AppKitEngine
import io.crosstoken.appkit.engine.coinbase.CoinbaseClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal fun engineModule() = module {

    single {
        ConnectionEventRepository(sharedPreferences = androidContext().getSharedPreferences("ConnectionEvents", Context.MODE_PRIVATE))
    }

    single {
        AppKitEngine(
            getSessionUseCase = get(),
            getSelectedChainUseCase = get(),
            deleteSessionDataUseCase = get(),
            saveSessionUseCase = get(),
            connectionEventRepository = get(),
            enableAnalyticsUseCase = get(),
            sendEventUseCase = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
        )
    }
    single {
        CoinbaseClient(
            context = get(),
            appMetaData = get()
        )
    }
}
