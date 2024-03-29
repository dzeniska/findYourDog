package com.dzenis_ska.findyourdog.ui.utils

import com.dzenis_ska.findyourdog.remoteModel.firebase.AdShelter
import com.dzenis_ska.findyourdog.remoteModel.firebase.AdShelterFilter

object FilterManager {

    fun createFilterVaccine(adShelter: AdShelter): Map<String, String>? {
        return adShelter.vaccination
    }
    fun createFilter(adShelter: AdShelter): AdShelterFilter {
        return AdShelterFilter(
            adShelter.time,
            adShelter.lat,
            adShelter.lng,
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

}