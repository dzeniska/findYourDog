package com.dzenis_ska.findyourdog.remoteModel


import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.concurrent.TimeUnit


val BASE_URL_RNDOM = "https://dog.ceo/api/"

interface ApiOnePhoto {
    @GET
    suspend fun getOnePhoto(
        @Url url: String
    ): ResponseBody


    companion object Factory {
        fun create(): ApiOnePhoto {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build()
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL_RNDOM)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
            return retrofit.create(ApiOnePhoto::class.java)
        }
    }
}

