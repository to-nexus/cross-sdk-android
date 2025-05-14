package io.crosstoken.sign.di

import io.crosstoken.android.internal.common.di.AndroidCommonDITags
import io.crosstoken.android.pulse.domain.InsertEventUseCase
import io.crosstoken.sign.engine.use_case.requests.OnPingUseCase
import io.crosstoken.sign.engine.use_case.requests.OnSessionAuthenticateUseCase
import io.crosstoken.sign.engine.use_case.requests.OnSessionDeleteUseCase
import io.crosstoken.sign.engine.use_case.requests.OnSessionEventUseCase
import io.crosstoken.sign.engine.use_case.requests.OnSessionExtendUseCase
import io.crosstoken.sign.engine.use_case.requests.OnSessionProposalUseCase
import io.crosstoken.sign.engine.use_case.requests.OnSessionRequestUseCase
import io.crosstoken.sign.engine.use_case.requests.OnSessionSettleUseCase
import io.crosstoken.sign.engine.use_case.requests.OnSessionUpdateUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun requestsModule() = module {

    single {
        OnSessionProposalUseCase(
            pairingController = get(),
            jsonRpcInteractor = get(),
            proposalStorageRepository = get(),
            resolveAttestationIdUseCase = get(),
            insertEventUseCase = get(),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }

    single {
        OnSessionAuthenticateUseCase(
            jsonRpcInteractor = get(),
            resolveAttestationIdUseCase = get(),
            logger = get(),
            pairingController = get(),
            insertTelemetryEventUseCase = get(),
            insertEventUseCase = get<InsertEventUseCase>(),
            clientId = get(named(AndroidCommonDITags.CLIENT_ID)),
        )
    }

    single {
        OnSessionSettleUseCase(
            proposalStorageRepository = get(),
            jsonRpcInteractor = get(),
            pairingController = get(),
            metadataStorageRepository = get(),
            sessionStorageRepository = get(),
            crypto = get(),
            selfAppMetaData = get(),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }

    single {
        OnSessionRequestUseCase(
            metadataStorageRepository = get(),
            sessionStorageRepository = get(),
            jsonRpcInteractor = get(),
            resolveAttestationIdUseCase = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            insertEventUseCase = get<InsertEventUseCase>(),
            clientId = get(named(AndroidCommonDITags.CLIENT_ID)),
        )
    }

    single { OnSessionDeleteUseCase(jsonRpcInteractor = get(), crypto = get(), sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single { OnSessionEventUseCase(jsonRpcInteractor = get(), sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single { OnSessionUpdateUseCase(jsonRpcInteractor = get(), sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single { OnSessionExtendUseCase(jsonRpcInteractor = get(), sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single { OnPingUseCase(jsonRpcInteractor = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }
}