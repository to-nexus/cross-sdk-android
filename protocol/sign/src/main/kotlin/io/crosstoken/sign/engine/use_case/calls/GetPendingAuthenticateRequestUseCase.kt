package io.crosstoken.sign.engine.use_case.calls

import io.crosstoken.android.internal.common.json_rpc.data.JsonRpcSerializer
import io.crosstoken.android.internal.common.storage.rpc.JsonRpcHistory
import io.crosstoken.sign.common.model.Request
import io.crosstoken.sign.common.model.vo.clientsync.session.SignRpc
import io.crosstoken.sign.common.model.vo.clientsync.session.params.SignParams
import io.crosstoken.sign.json_rpc.model.JsonRpcMethod
import io.crosstoken.sign.json_rpc.model.toRequest

internal class GetPendingAuthenticateRequestUseCase(
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) : GetPendingAuthenticateRequestUseCaseInterface {
    override suspend fun getPendingAuthenticateRequests(): List<Request<SignParams.SessionAuthenticateParams>> {
        return jsonRpcHistory.getListOfPendingRecords()
            .filter { record -> record.method == JsonRpcMethod.WC_SESSION_AUTHENTICATE }
            .mapNotNull { record -> serializer.tryDeserialize<SignRpc.SessionAuthenticate>(record.body)?.toRequest(record) }
    }
}

internal interface GetPendingAuthenticateRequestUseCaseInterface {
    suspend fun getPendingAuthenticateRequests(): List<Request<SignParams.SessionAuthenticateParams>>
}