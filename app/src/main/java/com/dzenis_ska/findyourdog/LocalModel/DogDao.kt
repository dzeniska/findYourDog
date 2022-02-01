package com.dzenis_ska.findyourdog.LocalModel


import androidx.room.*
import com.dzenis_ska.findyourdog.remoteModel.DogBreeds

@Dao
interface DogDao {

    @Insert
    suspend fun insertBreeds(breeds: MutableList<DogBreeds>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOneDog(newDog: DogBreeds)

    @Query("SELECT * FROM breeds")
    suspend fun getAllBreedes(): MutableList<DogBreeds>

    @Query("UPDATE breeds SET isFavorite = :isSelected WHERE id = :id")
    suspend fun updateOnePost(id: Long, isSelected: Int)

    @Query("UPDATE breeds SET note = :note WHERE id = :id")
    suspend fun updateOneNote(id: Long, note: String)

    @Query("SELECT * FROM breeds WHERE id = :id")
    suspend fun getOnePost(id:Long):DogBreeds

    @Query("SELECT * FROM breeds WHERE isFavorite = 1")
    suspend fun selectFavorites():MutableList<DogBreeds>

    @Query("SELECT * FROM breeds WHERE name LIKE :breed")
    suspend fun searchView(breed: kotlin.Array<kotlin.String>):MutableList<DogBreeds>

    @Insert
    suspend fun insertOnePost(newBreed: DogBreeds)

}