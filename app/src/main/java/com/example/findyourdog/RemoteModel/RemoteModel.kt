package com.example.findyourdog.RemoteModel

import android.util.Log
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

@Suppress("CAST_NEVER_SUCCEEDS")
class RemoteModel @Inject constructor() {
    val apiService = ApiService.create()
    val apiRandom = ApiRandomImage.create()
    val apiOnePhoto = ApiOnePhoto.create()

    //одного фото запрос
    suspend fun getOnePhoto(url: String): ResponseBody {
            var photo: ResponseBody? = null
        return try {
            photo = apiOnePhoto.getOnePhoto(url)
            Log.d("!!!ps", photo.toString())
             photo
        }catch (e: IOException){
            Log.d("!!!ps", e.message.toString())
            photo!!
        }
    }

    suspend fun getAllBreedd(): MutableList<DogBreeds> {
        return try {
            val breeds = apiService.getAllBreeds()
            breeds
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    suspend fun getImages(breed: String): ImgBreed {
        return try {
            val breed = apiRandom.getImages(breed.toString())
            breed
        } catch (e: Exception) {
            ImgBreed(listOf(), "")
        }
    }

    suspend fun getImagesDouble(breed: String, bybreed: String): ImgBreed {
        return try {

            val breed = apiRandom.getImagesDouble(breed, bybreed)
            breed
        } catch (e: Exception) {
            ImgBreed(listOf(), "")
        }
    }

    suspend fun getBreeds(): BreedOfDogListPhoto {
        return try {
            val breed = apiRandom.getBreeds()
            breed
        } catch (e: Exception) {
            val optionsList: Map<String, List<String>> = mapOf()
            BreedOfDogListPhoto(0, optionsList, "")
        }
    }
}