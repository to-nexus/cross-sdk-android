package io.crosstoken.sign.engine.use_case.requests

import io.crosstoken.android.internal.common.model.IrnParams
import io.crosstoken.android.internal.common.model.Tags
import io.crosstoken.android.internal.common.model.WCRequest
import io.crosstoken.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import io.crosstoken.android.internal.utils.thirtySeconds
import io.crosstoken.foundation.common.model.Ttl
import io.crosstoken.foundation.util.Logger
import kotlinx.coroutines.supervisorScope

internal class OnPingUseCase(private val jsonRpcInteractor: RelayJsonRpcInteractorInterface, private val logger: Logger) {

    suspend operator fun invoke(request: WCRequest) = supervisorScope {
        val irnParams = IrnParams(Tags.SESSION_PING_RESPONSE, Ttl(thirtySeconds), correlationId = request.id)
        logger.log("Session ping received on topic: ${request.topic}")
        jsonRpcInteractor.respondWithSuccess(request, irnParams)
    }
}