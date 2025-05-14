package io.crosstoken.appkit.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import io.crosstoken.android.internal.common.di.AndroidCommonDITags
import io.crosstoken.appkit.domain.RecentWalletsRepository
import io.crosstoken.appkit.domain.SessionRepository
import io.crosstoken.appkit.domain.model.Session
import io.crosstoken.appkit.domain.usecase.DeleteSessionDataUseCase
import io.crosstoken.appkit.domain.usecase.GetRecentWalletUseCase
import io.crosstoken.appkit.domain.usecase.GetSelectedChainUseCase
import io.crosstoken.appkit.domain.usecase.GetSessionUseCase
import io.crosstoken.appkit.domain.usecase.ObserveSelectedChainUseCase
import io.crosstoken.appkit.domain.usecase.ObserveSessionUseCase
import io.crosstoken.appkit.domain.usecase.SaveChainSelectionUseCase
import io.crosstoken.appkit.domain.usecase.SaveRecentWalletUseCase
import io.crosstoken.appkit.domain.usecase.SaveSessionUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session_store")

internal fun appKitModule() = module {

    single { RecentWalletsRepository(sharedPreferences = get()) }

    single { GetRecentWalletUseCase(repository = get()) }
    single { SaveRecentWalletUseCase(repository = get()) }

    single<PolymorphicJsonAdapterFactory<Session>> {
        PolymorphicJsonAdapterFactory.of(Session::class.java, "type")
            .withSubtype(Session.WalletConnect::class.java, "wcsession")
            .withSubtype(Session.Coinbase::class.java, "coinbase")
    }

    single<Moshi>(named(AppKitDITags.MOSHI)) {
        get<Moshi.Builder>(named(AndroidCommonDITags.MOSHI))
            .add(get<PolymorphicJsonAdapterFactory<Session>>())
            .build()
    }

    single(named(AppKitDITags.SESSION_DATA_STORE)) { androidContext().sessionDataStore }
    single { SessionRepository(sessionStore = get(named(AppKitDITags.SESSION_DATA_STORE)), moshi = get(named(AppKitDITags.MOSHI))) }

    single { GetSessionUseCase(repository = get()) }
    single { SaveSessionUseCase(repository = get()) }
    single { DeleteSessionDataUseCase(repository = get()) }
    single { SaveChainSelectionUseCase(repository = get()) }
    single { GetSelectedChainUseCase(repository = get()) }
    single { ObserveSessionUseCase(repository = get()) }
    single { ObserveSelectedChainUseCase(repository = get()) }

    includes(blockchainApiModule(), balanceRpcModule(), engineModule())
}
