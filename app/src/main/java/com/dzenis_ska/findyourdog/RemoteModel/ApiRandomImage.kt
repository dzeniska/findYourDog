package com.dzenis_ska.findyourdog.RemoteModel


import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit


val BASE_URL_RANDOM = "https://dog.ceo/api/"


interface ApiRandomImage {
    @GET("breed/{hound}/images")
    suspend fun getImages(
        @Path("hound") breed: String
    ): ImgBreed

    @GET("breed/{hound}/{byHound}/images")
    suspend fun getImagesDouble(
        @Path("hound") breed: String,
        @Path("byHound") bybreed: String
    ): ImgBreed

    @GET("breeds/list/all")
    suspend fun getBreeds(
    ):BreedOfDogListPhoto





    companion object Factory {
        fun create(): ApiRandomImage {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build()
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL_RANDOM)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
            return retrofit.create(ApiRandomImage::class.java)
        }
    }
}

