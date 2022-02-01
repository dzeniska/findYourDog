package com.dzenis_ska.findyourdog.LocalModel

import android.content.Context
import androidx.room.Room
import com.dzenis_ska.findyourdog.remoteModel.DogBreeds
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LocalModel @Inject constructor(@ApplicationContext context: Context) {
    private val database:DogDataBase = Room.databaseBuilder(
        context,
        DogDataBase::class.java, "dog_db"
    ).build()

    suspend fun insertBreeds(breeds: MutableList<DogBreeds>){
        database.dogDao().insertBreeds(breeds)
    }

    suspend fun getAllBreed(): MutableList<DogBreeds> {
       return  database.dogDao().getAllBreedes()
    }

    suspend fun insertFavoritePosts(id: Long, isSelected: Int){
        database.dogDao().updateOnePost(id, isSelected)
    }
    suspend fun insertNote(id: Long, note: String){
        database.dogDao().updateOneNote(id, note)
    }

    suspend fun selectFavorites(): MutableList<DogBreeds>{
        return database.dogDao().selectFavorites()
    }
    suspend fun searchView(breed: Array<String>):MutableList<DogBreeds>{
        return database.dogDao().searchView(breed)
    }

}