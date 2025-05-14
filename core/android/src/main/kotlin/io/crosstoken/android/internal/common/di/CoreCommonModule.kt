package io.crosstoken.android.internal.common.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.tinder.scarlet.utils.getRawType
import io.crosstoken.android.internal.common.JsonRpcResponse
import io.crosstoken.android.internal.common.adapter.ExpiryAdapter
import io.crosstoken.android.internal.common.adapter.JsonRpcResultAdapter
import io.crosstoken.android.internal.common.adapter.TagsAdapter
import io.crosstoken.android.internal.common.model.Expiry
import io.crosstoken.android.internal.common.model.Tags
import io.crosstoken.android.pulse.model.properties.Props
import io.crosstoken.foundation.di.FoundationDITags
import io.crosstoken.foundation.di.foundationCommonModule
import io.crosstoken.foundation.util.Logger
import org.koin.core.qualifier.named
import org.koin.dsl.module
import timber.log.Timber
import kotlin.reflect.jvm.jvmName

fun coreCommonModule() = module {

    includes(foundationCommonModule())

    single<PolymorphicJsonAdapterFactory<JsonRpcResponse>> {
        PolymorphicJsonAdapterFactory.of(JsonRpcResponse::class.java, "type")
            .withSubtype(JsonRpcResponse.JsonRpcResult::class.java, "result")
            .withSubtype(JsonRpcResponse.JsonRpcError::class.java, "error")
    }

    single<Moshi.Builder>(named(AndroidCommonDITags.MOSHI)) {
        get<Moshi>(named(FoundationDITags.MOSHI))
            .newBuilder()
            .add { type, _, moshi ->
                when (type.getRawType().name) {
                    Expiry::class.jvmName -> ExpiryAdapter
                    Tags::class.jvmName -> TagsAdapter
                    JsonRpcResponse.JsonRpcResult::class.jvmName -> JsonRpcResultAdapter(moshi)
                    else -> null
                }
            }
            .add(get<PolymorphicJsonAdapterFactory<JsonRpcResponse>>())
            .add(get<PolymorphicJsonAdapterFactory<Props>>())
    }

    single {
        Timber
    }

    single<Logger>(named(AndroidCommonDITags.LOGGER)) {
        object : Logger {
            override fun log(logMsg: String?) {
                get<Timber.Forest>().d(logMsg)
            }

            override fun log(throwable: Throwable?) {
                get<Timber.Forest>().d(throwable)
            }

            override fun error(errorMsg: String?) {
                get<Timber.Forest>().e(errorMsg)
            }

            override fun error(throwable: Throwable?) {
                get<Timber.Forest>().e(throwable)
            }
        }
    }
}