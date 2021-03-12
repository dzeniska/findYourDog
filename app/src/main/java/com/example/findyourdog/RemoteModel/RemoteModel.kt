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
            breeds
        } catch (e: Exception){
            mutableListOf()
        }
    }
    suspend fun getImages(breed: String): ImgBreed{
        return try {
            val breed = apiRandom.getImages(breed.toString())
            breed
        } catch (e: Exception){
            ImgBreed(listOf(), "")
        }
    }
    suspend fun getImagesDouble(breed: String, bybreed:String): ImgBreed{
        return try {

            val breed = apiRandom.getImagesDouble(breed, bybreed)
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
            val optionsList: Map<String, List<String>> = mapOf()
            BreedOfDogListPhoto(0, optionsList, "")
        }
    }
}