package com.example.findyourdog.RemoteModel

data class BreedOfDogListPhoto (
        val id:Int,
        val message: Map< String, List<String>>,
        val status: String
)