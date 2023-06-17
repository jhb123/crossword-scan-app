package com.jhb.crosswordScan.network


import okhttp3.RequestBody
import okhttp3.ResponseBody
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

    //@GET("hello")
    suspend fun getGuid(): String

    @GET("auth/resetPassword")
    suspend fun getResetPassword(@Query("email") apiKey: String): String

    @POST("puzzles/upload")
    suspend fun upload(@Header("Authorization") token : String, @Body request: RequestBody): ResponseBody
//    suspend fun upload(@Body request: RequestBody): ResponseBody

    @POST("puzzles/search")
    suspend fun search(
//        @Header("Authorization") token : String,
        @Header("Authorization") token : String,
        @Body request: RequestBody
    ): ResponseBody
//    suspend fun upload(@Body request: RequestBody): ResponseBody


    @POST("auth/resetPassword")
    suspend fun postResetPassword(@Body request: RequestBody): ResponseBody

    @POST("auth/register")
    suspend fun register(@Body request: RequestBody): ResponseBody

    @POST("auth/login2")
    suspend fun login(@Body requestBody: RequestBody): ResponseBody

}