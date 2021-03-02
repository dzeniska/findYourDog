package com.example.findyourdog.LocalModel

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.findyourdog.RemoteModel.DogBreeds
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LocalModel @Inject constructor(@ApplicationContext context: Context) {
    private val database:DogDataBase = Room.databaseBuilder(
        context,
        DogDataBase::class.java, "dog_db"
    ).build()

    suspend fun insertBreeds(breeds: MutableList<DogBreeds>){
        Log.d("!!!locmod", breeds.toString())
        database.dogDao().insertBreeds(breeds)
    }
    suspend fun insertOneDog(dog: DogBreeds){
        database.dogDao().insertOneDog(dog)
    }

    suspend fun getAllBreed(): MutableList<DogBreeds> {
        Log.d("!!!error", "data.size.toString()")
        val d = database.dogDao().getAllBreedes()
       return  database.dogDao().getAllBreedes()
    }

    suspend fun insertFavoritePosts(id: Long, isSelected: Int){
        database.dogDao().updateOnePost(id, isSelected)
    }
    suspend fun insertNote(id: Long, note: String){
        database.dogDao().updateOneNote(id, note)
    }
    suspend fun getOnePost(id : Long):DogBreeds{
        return database.dogDao().getOnePost(id)
    }
    suspend fun selectFavorites(): MutableList<DogBreeds>{
        return database.dogDao().selectFavorites()
    }
    suspend fun searchView(breed: Array<String>):MutableList<DogBreeds>{
        return database.dogDao().searchView(breed)
    }
    suspend fun insertOnePost(newBreed: DogBreeds){
        database.dogDao().insertOnePost(newBreed)
    }
}