package com.dzenis_ska.findyourdog.ui.utils

import com.dzenis_ska.findyourdog.remoteModel.firebase.AdShelter

object FilterManager {

    fun createFilterVaccine(adShelter: AdShelter): Map<String, String>? {
        return adShelter.vaccination
    }
    fun createFilter(adShelter: AdShelter): AdShelterFilter {
        return AdShelterFilter(
            adShelter.email,
            adShelter.uid,
            adShelter.time,
            createLoc(adShelter.lat),
            createLoc(adShelter.lng),
            "${adShelter.lat ?: 0.0}_${adShelter.lng ?: 0.0}",
            "${adShelter.breed}_${adShelter.gender}_${adShelter.size}_${adShelter.time}",
            "${adShelter.breed}_${adShelter.gender}_${adShelter.time}",
            "${adShelter.breed}_${adShelter.size}_${adShelter.time}",
            "${adShelter.gender}_${adShelter.size}_${adShelter.time}",
            "${adShelter.breed}_${adShelter.time}",
            "${adShelter.gender}_${adShelter.time}",
            "${adShelter.size}_${adShelter.time}"
        )
    }
    fun createLoc(loc: Double?): String{
        String.format("%.3f", loc).also {
            return if (it.startsWith('-'))
                when (it.length) {
                    7 -> "-0${it.substringAfter('-')}"
                    6 -> "-00${it.substringAfter('-')}"
                    else -> "-${it.substringAfter('-')}"
                }
            else
                when (it.length) {
                    6 -> "0$it"
                    5 -> "00$it"
                    else -> it
                }
        }
    }

}
data class AdShelterFilter(
    val email: String? = null,
    val uid: String? = null,
    val time: String? = null,
    val lat: String? = null,
    val lng: String? = null,
    val lat_lng: String? = null,
    val breed_gender_size_time: String? = null,
    val breed_gender_time: String? = null,
    val breed_size_time: String? = null,
    val gender_size_time: String? = null,
    val breed_time: String? = null,
    val gender_time: String? = null,
    val size_time: String? = null
)