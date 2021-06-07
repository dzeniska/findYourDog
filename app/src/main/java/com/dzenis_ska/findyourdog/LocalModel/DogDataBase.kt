package com.dzenis_ska.findyourdog.LocalModel

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dzenis_ska.findyourdog.RemoteModel.DogBreeds

@Database(entities = arrayOf(DogBreeds::class/*, Weight::class, Height::class, Image::class*/), version = 1)
abstract class DogDataBase: RoomDatabase() {
        abstract fun dogDao(): DogDao
}