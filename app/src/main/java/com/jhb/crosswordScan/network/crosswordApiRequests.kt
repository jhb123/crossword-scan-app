package com.jhb.crosswordScan.network

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "http://192.168.0.34:5000"
private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

// you only need one instance of retrofit, and its expensive. Therefore,
// it makes sense to have it be a singleton
object CrosswordApi {

    val retrofitService : CrosswordApiService by lazy {
        retrofit.create(CrosswordApiService::class.java)
    }
}

interface CrosswordApiService {
    @GET("hello")
    suspend fun getHello(): String

    @GET("auth/resetPassword")
    suspend fun getResetPassword(@Query("email") apiKey: String): String

}