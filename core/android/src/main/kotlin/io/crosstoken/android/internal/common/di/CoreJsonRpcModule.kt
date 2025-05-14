package io.crosstoken.android.internal.common.di

import com.squareup.moshi.Moshi
import io.crosstoken.android.internal.common.json_rpc.data.JsonRpcSerializer
import io.crosstoken.android.internal.common.json_rpc.domain.link_mode.LinkModeJsonRpcInteractor
import io.crosstoken.android.internal.common.json_rpc.domain.link_mode.LinkModeJsonRpcInteractorInterface
import io.crosstoken.android.internal.common.json_rpc.domain.relay.RelayJsonRpcInteractor
import io.crosstoken.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import io.crosstoken.android.internal.common.model.type.SerializableJsonRpc
import io.crosstoken.android.pairing.model.PairingJsonRpcMethod
import io.crosstoken.android.pairing.model.PairingRpc
import io.crosstoken.utils.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.reflect.KClass


@JvmSynthetic
fun coreJsonRpcModule() = module {

    single<RelayJsonRpcInteractorInterface> {
        RelayJsonRpcInteractor(
            relay = get(),
            chaChaPolyCodec = get(),
            jsonRpcHistory = get(),
            pushMessageStorage = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            backoffStrategy = get()
        )
    }

    addSerializerEntry(PairingRpc.PairingPing::class)
    addSerializerEntry(PairingRpc.PairingDelete::class)

    addDeserializerEntry(PairingJsonRpcMethod.WC_PAIRING_PING, PairingRpc.PairingPing::class)
    addDeserializerEntry(PairingJsonRpcMethod.WC_PAIRING_DELETE, PairingRpc.PairingDelete::class)

    factory {
        JsonRpcSerializer(
            serializerEntries = getAll<KClass<SerializableJsonRpc>>().toSet(),
            deserializerEntries = getAll<Pair<String, KClass<*>>>().toMap(),
            jsonAdapterEntries = getAll<JsonAdapterEntry<*>>().toSet(),
            moshiBuilder = get<Moshi.Builder>(named(AndroidCommonDITags.MOSHI))
        )
    }

    single<LinkModeJsonRpcInteractorInterface> {
        LinkModeJsonRpcInteractor(
            chaChaPolyCodec = get(),
            jsonRpcHistory = get(),
            context = androidContext()
        )
    }
}