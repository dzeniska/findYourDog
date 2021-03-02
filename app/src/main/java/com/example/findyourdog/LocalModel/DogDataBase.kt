package com.example.findyourdog.LocalModel

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.findyourdog.RemoteModel.DogBreeds
import com.example.findyourdog.RemoteModel.Height
import com.example.findyourdog.RemoteModel.Image
import com.example.findyourdog.RemoteModel.Weight

@Database(entities = arrayOf(DogBreeds::class/*, Weight::class, Height::class, Image::class*/), version = 1)
abstract class DogDataBase: RoomDatabase() {
        abstract fun dogDao(): DogDao
}