@file:JvmSynthetic

package io.crosstoken.android.pulse.data

import io.crosstoken.android.pulse.model.Event
import io.crosstoken.foundation.util.Logger
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface PulseService {
    @Headers("Content-Type: application/json")
    @POST("/e")
    suspend fun sendEvent(@Header("x-sdk-type") sdkType: String, @Body body: Event): Response<Unit>

    @Headers("Content-Type: application/json")
    @POST("/batch")
    suspend fun sendEventBatch(@Header("x-sdk-type") sdkType: String, @Body body: List<Event>): Response<Unit>
}

class CrossPulseService(private val logger: Logger) : PulseService {

    override suspend fun sendEvent(sdkType: String, body: Event): Response<Unit> {
        logger.log("sendEvent: $body")
        return Response.success(Unit)
    }

    override suspend fun sendEventBatch(sdkType: String, body: List<Event>): Response<Unit> {
        logger.log("sendEventBatch: $body")
        return Response.success(Unit)
    }
}