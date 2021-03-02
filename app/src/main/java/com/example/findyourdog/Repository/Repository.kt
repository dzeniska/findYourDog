package com.example.findyourdog.Repository

import android.util.Log
import com.example.findyourdog.LocalModel.LocalModel
import com.example.findyourdog.RemoteModel.*
import javax.inject.Inject


class Repository @Inject constructor(val remoteModel: RemoteModel, val localModel: LocalModel)
{
    suspend fun getAllBreeds(): MutableList<DogBreeds>{
        var breedsList = localModel.getAllBreed()
        return if (breedsList.isEmpty()){
            breedsList = remoteModel.getAllBreedd()
            Log.d("!!!dl", breedsList.toString())
            localModel.insertBreeds(breedsList)
            breedsList
        } else {
            Log.d("!!!dlelse", breedsList.toString())
            breedsList
        }
    }
    suspend fun insOneDog(dog: DogBreeds){

        localModel.insertOneDog(dog)
    }
    suspend fun saveFavoriteData(id: Long, isSelected: Int){
        localModel.insertFavoritePosts(id, isSelected)
    }
    suspend fun saveNote(id: Long, note: String){
        localModel.insertNote(id, note)
    }

    suspend fun getOnePost(id: Long): DogBreeds {
        var post = localModel.getOnePost(id)
        if (post == null) {
            post = remoteModel.getAllBreedd().first { it.id == id }
        }
        return post
    }

    suspend fun selectFavorites(): MutableList<DogBreeds>{
        return localModel.selectFavorites()
    }
    suspend fun getImgBreeds(breed: String): ImgBreed{
        return remoteModel.getImages(breed)
    }
    suspend fun searchView(breed: Array<String>):MutableList<DogBreeds>{
        return localModel.searchView(breed)
    }
    suspend fun insertOnePost(newBreeds: DogBreeds) {
        localModel.insertOnePost(newBreeds)
    }
    suspend fun getBreeds():BreedOfDogListPhoto{
            return remoteModel.getBreeds()

    }

}