package com.jhb.crosswordScan.network


import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

private const val BASE_URL = "http://192.168.0.34:5000"
private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(ScalarsConverterFactory.create())
    .addConverterFactory(GsonConverterFactory.create())
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

    @POST("auth/register")
    suspend fun register(@Body request: CrosswordAppRequest): Call<Unit>

    @POST("auth/login2")
    suspend fun login(@Body requestBody: RequestBody): ResponseBody

}