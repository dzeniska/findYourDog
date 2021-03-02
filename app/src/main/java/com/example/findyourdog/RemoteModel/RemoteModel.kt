package com.example.findyourdog.RemoteModel

import android.util.Log
import java.lang.Exception
import javax.inject.Inject

class RemoteModel  @Inject constructor(){
    val apiService = ApiService.create()
    val apiRandom = ApiRandomImage.create()

    suspend fun getAllBreedd(): MutableList<DogBreeds>{
        return try {
            val breeds = apiService.getAllBreeds()
            Log.d("!!!RM", breeds.toString())
            breeds
        } catch (e: Exception){
            mutableListOf()
        }
    }
    suspend fun getImages(breed: String): ImgBreed{
        return try {
            val breed = apiRandom.getImages(breed)
            breed
        } catch (e: Exception){
            ImgBreed(listOf(), "")
        }
    }
    suspend fun getBreeds(): BreedOfDogListPhoto{
        return try {
            val breed = apiRandom.getBreeds()
            breed
        } catch (e: Exception){
            apiRandom.getBreeds()
        }
    }
}