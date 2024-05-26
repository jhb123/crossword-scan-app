package com.jhb.crosswordScan.network


import com.jhb.crosswordScan.BuildConfig
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*


class MyCookieJar : CookieJar {
    private var cookies: List<Cookie>? = null
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        this.cookies = cookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookies ?: ArrayList()
    }
}
private val cookieJar = MyCookieJar()

private val client = OkHttpClient.Builder()
    .cookieJar(cookieJar)
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl(BuildConfig.API_URL)
    .addConverterFactory(ScalarsConverterFactory.create())
    .addConverterFactory(GsonConverterFactory.create())
    .client(client)
    .build()


// you only need one instance of retrofit, and its expensive. Therefore,
// it makes sense to have it be a singleton
object CrosswordApi {

    val retrofitService : CrosswordApiService by lazy {
        retrofit.create(CrosswordApiService::class.java)
    }

}

interface CrosswordApiService {

    @GET("auth/resetPassword")
    suspend fun getResetPassword(@Query("email") apiKey: String): String

    @POST("puzzle/add")
    suspend fun upload(@Body request: RequestBody): ResponseBody
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

    @POST("sign-up")
    suspend fun register(@Body request: RequestBody): ResponseBody

    @POST("log-in")
    suspend fun login(@Body requestBody: RequestBody): ResponseBody

    @POST("log-out")
    suspend fun logOut(): ResponseBody

    suspend fun getGuid(): String
}