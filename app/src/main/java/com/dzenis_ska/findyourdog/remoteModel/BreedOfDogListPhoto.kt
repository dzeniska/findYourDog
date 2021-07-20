package com.dzenis_ska.findyourdog.remoteModel

data class BreedOfDogListPhoto (
        val id:Int,
        val message: Map< String, List<String>>,
        val status: String
)