package com.example.findyourdog.RemoteModel

import android.util.Log
import android.widget.Toast
import okhttp3.ResponseBody
import org.json.JSONObject
import javax.inject.Inject


@Suppress("CAST_NEVER_SUCCEEDS")
class RemoteModel @Inject constructor() {
    val apiService = ApiService.create()
    val apiRandom = ApiRandomImage.create()
    val apiOnePhoto = ApiOnePhoto.create()


    //одного фото запрос
    suspend fun getOnePhoto(url: String): ByteArray{
        var photo: ByteArray
        val photoString = ""
        return try {
            photo = apiOnePhoto.getOnePhoto(url).bytes()
            Log.d("!!!ps", photo.toString())
            photo
        } catch (e: Exception) {
            Log.d("!!!ps", e.toString())
            photo = photoString.toByteArray()
            photo
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