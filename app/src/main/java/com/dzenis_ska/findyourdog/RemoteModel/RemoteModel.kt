package com.dzenis_ska.findyourdog.RemoteModel


import android.util.Log
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
            return photo
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
            apiRandom.getImages(breed)
        } catch (e: Exception) {
            ImgBreed(listOf(), "")
        }
    }

    suspend fun getImagesDouble(breed: String, bybreed: String): ImgBreed {
        return try {
            apiRandom.getImagesDouble(breed, bybreed)
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