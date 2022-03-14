package com.dzenis_ska.findyourdog.ui.utils

import android.net.Uri

object SortListPhoto {

    fun listPhotoForDel(
        listPhoto: ArrayList<String>,
        arrayPhoto: MutableList<Uri>
    ): ArrayList<String> {

//        val list = arrayPhoto.filterNot { it.toString().contains("content") }

        val listDel = arrayListOf<String>()
        listDel.addAll(listPhoto)
        arrayPhoto.forEach { uri ->
            listPhoto.forEach {
                if (it.contains(uri.toString())) listDel.remove(it)
            }
        }

        return listDel
    }
}