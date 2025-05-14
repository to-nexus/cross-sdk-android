package io.crosstoken.sign.json_rpc.domain

import io.crosstoken.android.internal.common.json_rpc.data.JsonRpcSerializer
import io.crosstoken.android.internal.common.json_rpc.model.JsonRpcHistoryRecord
import io.crosstoken.android.internal.common.storage.rpc.JsonRpcHistory
import io.crosstoken.sign.common.model.Request
import io.crosstoken.sign.common.model.vo.clientsync.session.SignRpc
import io.crosstoken.sign.common.model.vo.clientsync.session.params.SignParams
import io.crosstoken.sign.json_rpc.model.toRequest

internal class GetPendingJsonRpcHistoryEntryByIdUseCase(
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) {

    operator fun invoke(id: Long): Request<SignParams.SessionRequestParams>? {
        val record: JsonRpcHistoryRecord? = jsonRpcHistory.getPendingRecordById(id)
        var entry: Request<SignParams.SessionRequestParams>? = null

        if (record != null) {
            val sessionRequest: SignRpc.SessionRequest? = serializer.tryDeserialize<SignRpc.SessionRequest>(record.body)
            if (sessionRequest != null) {
                entry = record.toRequest(sessionRequest.params)
            }
        }

        return entry
    }
}