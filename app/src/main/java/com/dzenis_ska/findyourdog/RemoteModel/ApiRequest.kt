package com.dzenis_ska.findyourdog.RemoteModel


import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit


val BASE_URL = "https://api.thedogapi.com/v1/"

interface ApiService {
    @Headers("x-api-key:c102c232-9aec-4038-80bd-7eb6eeba21a7")
    @GET("breeds")
    suspend fun getAllBreeds(
        //@Query("q") cityname: String
    ): MutableList<DogBreeds>



    companion object Factory {
        fun create(): ApiService {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build()
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
            return retrofit.create(ApiService::class.java)
        }
    }
}