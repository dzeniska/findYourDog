package com.example.findyourdog.RemoteModel

import androidx.room.*

@Entity(tableName = "breeds")
data class DogBreeds(
    @Embedded val weight: Weight,
    @Embedded val height: Height,
    @PrimaryKey val id: Long,
    val name: String?,
    var isFavorite:Int = 0,
    var ind:Int = 0,
    var note: String?,
//    val photo:ByteArray?,
    val photo: String?,
    val bredFor: String?,
    val breedGroup: String?,
    val lifeSpan: String?,
    val temperament: String?,
    val origin: String?,
    val referenceImageId: String?,
    @Embedded val image: Image,
    val countryCode: String?,
    val description: String?,
    val history: String?
)
data class Weight(
    val imperial: String?,
    val metric: String?
)
data class Height(
    @ColumnInfo(name = "imperial_height") val imperial: String?,
    @ColumnInfo(name = "metric_height") val metric: String?
)
data class Image(
    @ColumnInfo(name = "name_id") val id: String?,
    val width: Long?,
    val height: Long?,
    val url: String?
)




