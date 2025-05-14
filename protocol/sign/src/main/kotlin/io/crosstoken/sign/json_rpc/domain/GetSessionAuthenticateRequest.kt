package io.crosstoken.sign.json_rpc.domain

import io.crosstoken.android.internal.common.json_rpc.data.JsonRpcSerializer
import io.crosstoken.android.internal.common.json_rpc.model.JsonRpcHistoryRecord
import io.crosstoken.android.internal.common.storage.rpc.JsonRpcHistory
import io.crosstoken.sign.common.model.Request
import io.crosstoken.sign.common.model.vo.clientsync.session.SignRpc
import io.crosstoken.sign.common.model.vo.clientsync.session.params.SignParams
import io.crosstoken.sign.json_rpc.model.toRequest

internal class GetSessionAuthenticateRequest(
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer
) {
    internal operator fun invoke(id: Long): Request<SignParams.SessionAuthenticateParams>? {
        val record: JsonRpcHistoryRecord? = jsonRpcHistory.getRecordById(id)
        var entry: Request<SignParams.SessionAuthenticateParams>? = null

        if (record != null) {
            val authRequest: SignRpc.SessionAuthenticate? = serializer.tryDeserialize<SignRpc.SessionAuthenticate>(record.body)
            if (authRequest != null) {
                entry = record.toRequest(authRequest.params)
            }
        }

        return entry
    }
}