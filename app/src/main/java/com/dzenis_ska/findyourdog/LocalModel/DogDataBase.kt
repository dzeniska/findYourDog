package com.dzenis_ska.findyourdog.LocalModel

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dzenis_ska.findyourdog.remoteModel.DogBreeds

@Database(entities = [DogBreeds::class], version = 1)
abstract class DogDataBase: RoomDatabase() {
        abstract fun dogDao(): DogDao
}