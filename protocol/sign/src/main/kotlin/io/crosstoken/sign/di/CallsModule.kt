package io.crosstoken.sign.di

import io.crosstoken.android.internal.common.di.AndroidCommonDITags
import io.crosstoken.android.internal.common.json_rpc.domain.link_mode.LinkModeJsonRpcInteractorInterface
import io.crosstoken.android.internal.common.model.Tags
import io.crosstoken.android.push.notifications.DecryptMessageUseCaseInterface
import io.crosstoken.sign.engine.use_case.calls.ApproveSessionAuthenticateUseCase
import io.crosstoken.sign.engine.use_case.calls.ApproveSessionAuthenticateUseCaseInterface
import io.crosstoken.sign.engine.use_case.calls.ApproveSessionUseCase
import io.crosstoken.sign.engine.use_case.calls.ApproveSessionUseCaseInterface
import io.crosstoken.sign.engine.use_case.calls.DecryptSignMessageUseCase
import io.crosstoken.sign.engine.use_case.calls.DisconnectSessionUseCase
import io.crosstoken.sign.engine.use_case.calls.DisconnectSessionUseCaseInterface
import io.crosstoken.sign.engine.use_case.calls.EmitEventUseCase
import io.crosstoken.sign.engine.use_case.calls.EmitEventUseCaseInterface
import io.crosstoken.sign.engine.use_case.calls.ExtendSessionUseCase
import io.crosstoken.sign.engine.use_case.calls.ExtendSessionUseCaseInterface
import io.crosstoken.sign.engine.use_case.calls.FormatAuthenticateMessageUseCase
import io.crosstoken.sign.engine.use_case.calls.FormatAuthenticateMessageUseCaseInterface
import io.crosstoken.sign.engine.use_case.calls.GetListOfVerifyContextsUseCase
import io.crosstoken.sign.engine.use_case.calls.GetListOfVerifyContextsUseCaseInterface
import io.crosstoken.sign.engine.use_case.calls.GetNamespacesFromReCaps
import io.crosstoken.sign.engine.use_case.calls.GetPairingForSessionAuthenticateUseCase
import io.crosstoken.sign.engine.use_case.calls.GetPairingsUseCase
import io.crosstoken.sign.engine.use_case.calls.GetPairingsUseCaseInterface
import io.crosstoken.sign.engine.use_case.calls.GetSessionProposalsUseCase
import io.crosstoken.sign.engine.use_case.calls.GetSessionProposalsUseCaseInterface
import io.crosstoken.sign.engine.use_case.calls.GetSessionsUseCase
import io.crosstoken.sign.engine.use_case.calls.GetSessionsUseCaseInterface
import io.crosstoken.sign.engine.use_case.calls.GetVerifyContextByIdUseCase
import io.crosstoken.sign.engine.use_case.calls.GetVerifyContextByIdUseCaseInterface
import io.crosstoken.sign.engine.use_case.calls.PairUseCase
import io.crosstoken.sign.engine.use_case.calls.PairUseCaseInterface
import io.crosstoken.sign.engine.use_case.calls.PingUseCase
import io.crosstoken.sign.engine.use_case.calls.PingUseCaseInterface
import io.crosstoken.sign.engine.use_case.calls.ProposeSessionUseCase
import io.crosstoken.sign.engine.use_case.calls.ProposeSessionUseCaseInterface
import io.crosstoken.sign.engine.use_case.calls.RejectSessionAuthenticateUseCase
import io.crosstoken.sign.engine.use_case.calls.RejectSessionAuthenticateUseCaseInterface
import io.crosstoken.sign.engine.use_case.calls.RejectSessionUseCase
import io.crosstoken.sign.engine.use_case.calls.RejectSessionUseCaseInterface
import io.crosstoken.sign.engine.use_case.calls.RespondSessionRequestUseCase
import io.crosstoken.sign.engine.use_case.calls.RespondSessionRequestUseCaseInterface
import io.crosstoken.sign.engine.use_case.calls.SessionAuthenticateUseCase
import io.crosstoken.sign.engine.use_case.calls.SessionAuthenticateUseCaseInterface
import io.crosstoken.sign.engine.use_case.calls.SessionRequestUseCase
import io.crosstoken.sign.engine.use_case.calls.SessionRequestUseCaseInterface
import io.crosstoken.sign.engine.use_case.calls.SessionUpdateUseCase
import io.crosstoken.sign.engine.use_case.calls.SessionUpdateUseCaseInterface
import io.crosstoken.sign.json_rpc.domain.GetPendingRequestsUseCaseByTopic
import io.crosstoken.sign.json_rpc.domain.GetPendingRequestsUseCaseByTopicInterface
import io.crosstoken.sign.json_rpc.domain.GetPendingSessionRequestByTopicUseCase
import io.crosstoken.sign.json_rpc.domain.GetPendingSessionRequestByTopicUseCaseInterface
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun callsModule() = module {

    single<ProposeSessionUseCaseInterface> {
        ProposeSessionUseCase(
            jsonRpcInteractor = get(),
            crypto = get(),
            selfAppMetaData = get(),
            proposalStorageRepository = get(),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }

    single<SessionAuthenticateUseCaseInterface> {
        SessionAuthenticateUseCase(
            jsonRpcInteractor = get(),
            crypto = get(),
            selfAppMetaData = get(),
            authenticateResponseTopicRepository = get(),
            proposeSessionUseCase = get(),
            getPairingForSessionAuthenticate = get(),
            getNamespacesFromReCaps = get(),
            linkModeJsonRpcInteractor = get<LinkModeJsonRpcInteractorInterface>(),
            linkModeStorageRepository = get(),
            insertEventUseCase = get(),
            clientId = get(named(AndroidCommonDITags.CLIENT_ID)),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }

    single<PairUseCaseInterface> { PairUseCase(pairingInterface = get()) }

    single<ApproveSessionUseCaseInterface> {
        ApproveSessionUseCase(
            proposalStorageRepository = get(),
            selfAppMetaData = get(),
            crypto = get(),
            jsonRpcInteractor = get(),
            metadataStorageRepository = get(),
            sessionStorageRepository = get(),
            verifyContextStorageRepository = get(),
            insertEventUseCase = get(),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }

    single<ApproveSessionAuthenticateUseCaseInterface> {
        ApproveSessionAuthenticateUseCase(
            jsonRpcInteractor = get(),
            crypto = get(),
            cacaoVerifier = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            verifyContextStorageRepository = get(),
            getPendingSessionAuthenticateRequest = get(),
            selfAppMetaData = get(),
            sessionStorageRepository = get(),
            metadataStorageRepository = get(),
            insertTelemetryEventUseCase = get(),
            insertEventUseCase = get(),
            clientId = get(named(AndroidCommonDITags.CLIENT_ID)),
            linkModeJsonRpcInteractor = get<LinkModeJsonRpcInteractorInterface>()
        )
    }

    single<RejectSessionAuthenticateUseCaseInterface> {
        RejectSessionAuthenticateUseCase(
            jsonRpcInteractor = get(),
            crypto = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            verifyContextStorageRepository = get(),
            getPendingSessionAuthenticateRequest = get(),
            linkModeJsonRpcInteractor = get<LinkModeJsonRpcInteractorInterface>(),
            clientId = get(named(AndroidCommonDITags.CLIENT_ID)),
            insertEventUseCase = get()
        )
    }

    single<RejectSessionUseCaseInterface> {
        RejectSessionUseCase(
            verifyContextStorageRepository = get(),
            proposalStorageRepository = get(),
            jsonRpcInteractor = get(),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }

    single<SessionUpdateUseCaseInterface> { SessionUpdateUseCase(jsonRpcInteractor = get(), sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single<SessionRequestUseCaseInterface> {
        SessionRequestUseCase(
            jsonRpcInteractor = get(),
            sessionStorageRepository = get(),
            linkModeJsonRpcInteractor = get(),
            metadataStorageRepository = get(),
            insertEventUseCase = get(),
            clientId = get(named(AndroidCommonDITags.CLIENT_ID)),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            tvf = get(),
            walletServiceFinder = get(),
            walletServiceRequester = get()
        )
    }

    single<RespondSessionRequestUseCaseInterface> {
        RespondSessionRequestUseCase(
            jsonRpcInteractor = get(),
            verifyContextStorageRepository = get(),
            sessionStorageRepository = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            getPendingJsonRpcHistoryEntryByIdUseCase = get(),
            linkModeJsonRpcInteractor = get(),
            metadataStorageRepository = get(),
            insertEventUseCase = get(),
            clientId = get(named(AndroidCommonDITags.CLIENT_ID)),
            tvf = get()
        )
    }

    single<DecryptMessageUseCaseInterface>(named(AndroidCommonDITags.DECRYPT_SIGN_MESSAGE)) {
        val useCase = DecryptSignMessageUseCase(
            codec = get(),
            serializer = get(),
            metadataRepository = get(),
            pushMessageStorage = get(),
        )

        get<MutableMap<String, DecryptMessageUseCaseInterface>>(named(AndroidCommonDITags.DECRYPT_USE_CASES))[Tags.SESSION_PROPOSE.id.toString()] = useCase
        useCase
    }

    single<PingUseCaseInterface> { PingUseCase(sessionStorageRepository = get(), jsonRpcInteractor = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single<EmitEventUseCaseInterface> { EmitEventUseCase(jsonRpcInteractor = get(), sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single<ExtendSessionUseCaseInterface> { ExtendSessionUseCase(jsonRpcInteractor = get(), sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single<DisconnectSessionUseCaseInterface> { DisconnectSessionUseCase(jsonRpcInteractor = get(), sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single<GetSessionsUseCaseInterface> { GetSessionsUseCase(sessionStorageRepository = get(), metadataStorageRepository = get(), selfAppMetaData = get()) }

    single<GetPairingsUseCaseInterface> { GetPairingsUseCase(pairingInterface = get()) }

    single { GetPairingForSessionAuthenticateUseCase(pairingProtocol = get()) }

    single { GetNamespacesFromReCaps() }

    single<GetPendingRequestsUseCaseByTopicInterface> { GetPendingRequestsUseCaseByTopic(serializer = get(), jsonRpcHistory = get()) }

    single<GetPendingSessionRequestByTopicUseCaseInterface> { GetPendingSessionRequestByTopicUseCase(jsonRpcHistory = get(), serializer = get(), metadataStorageRepository = get()) }

    single<GetSessionProposalsUseCaseInterface> { GetSessionProposalsUseCase(proposalStorageRepository = get()) }

    single<GetVerifyContextByIdUseCaseInterface> { GetVerifyContextByIdUseCase(verifyContextStorageRepository = get()) }

    single<GetListOfVerifyContextsUseCaseInterface> { GetListOfVerifyContextsUseCase(verifyContextStorageRepository = get()) }

    single<FormatAuthenticateMessageUseCaseInterface> { FormatAuthenticateMessageUseCase() }
}