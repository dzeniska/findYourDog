package com.dzenis_ska.findyourdog.remoteModel.firebase

data class AdShelter(
    val name: String? = null,
    val tel: String? = null,
    val lat: String? = null,
    val lng: String? = null,
    val description: String? = null,

    val photoes: ArrayList<String>? = null,

    val price: String? = null,
    val key: String? = null,
    val uid: String? = null,
    val markerColor: Float? = null,

    var viewsCounter: String = "0"
)
