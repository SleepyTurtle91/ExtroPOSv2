package com.extrotarget.extroposv2.core.network.api.autocount

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface AutoCountApi {

    @FormUrlEncoded
    @POST("Token")
    suspend fun login(
        @Field("grant_type") grantType: String = "password",
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<AutoCountAuthResponse>

    @POST("api/CashSale/Save")
    suspend fun saveCashSale(
        @Header("Authorization") token: String,
        @Body cashSale: AutoCountCashSale
    ): Response<AutoCountSyncResponse>
}
