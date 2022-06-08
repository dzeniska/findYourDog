package com.dzenis_ska.findyourdog.remoteModel.firebase


data class AdShelter(
    val name: String? = null,
    val tel: String? = null,
    val gender: String? = null,
    val size: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val description: String? = null,
    val breed: String? = null,
    val email: String? = null,

    val vaccination: Map<String, String>? = null,
    val photoes: ArrayList<String>? = null,

    val price: String? = null,
    val key: String? = null,
    val uid: String? = null,
    val markerColor: Float? = null,
    val time: String = "0",
    var viewsCounter: String = "0",
    var callsCounter: String = "0",
    var isFav: Boolean = false,
    var favCounter: String = "0",
)
