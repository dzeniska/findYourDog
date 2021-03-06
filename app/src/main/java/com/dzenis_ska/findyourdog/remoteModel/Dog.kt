package com.dzenis_ska.findyourdog.remoteModel

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dogs")
data class Dog(
    val photo:ByteArray?,
    @PrimaryKey val breedOfDog: String,
    val description: String
) {

}