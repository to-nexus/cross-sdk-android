package io.crosstoken.appkit.data.network

import io.crosstoken.appkit.data.json_rpc.balance.BalanceRequest
import io.crosstoken.appkit.data.json_rpc.balance.BalanceRpcResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

internal interface BalanceService {

    @POST
    suspend fun getBalance(
        @Url url: String,
        @Body body: BalanceRequest
    ): Response<BalanceRpcResponse>
}
