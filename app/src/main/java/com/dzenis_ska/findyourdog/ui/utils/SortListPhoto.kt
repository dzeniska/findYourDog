package com.dzenis_ska.findyourdog.ui.utils

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SortListPhoto {

    suspend fun istPhotoOld(arrayPhoto: MutableList<Uri>): ArrayList<String> = withContext(Dispatchers.IO) {
        val list = arrayPhoto.filterNot{it.toString().contains("content")}
        val listOld = arrayListOf<String>()
        list.forEach {
            listOld.add(it.toString())
        }
        return@withContext listOld
    }

    fun listPhotoForDel(listPhoto: ArrayList<String>, arrayPhoto: MutableList<Uri>): ArrayList<String> {

        val list = arrayPhoto.filterNot{it.toString().contains("content")}
        val listDel = arrayListOf<String>()
        listDel.addAll(listPhoto)
        list.forEach {uri->
            Log.d("!!!sortLists3", "${uri}")
            listPhoto.forEach {
                if(it.contains(uri.toString())) listDel.remove(it)
            }
        }
        val listNameDel = arrayListOf<String>()
        listDel.forEach {
            val index = it.indexOf('_')
            val text = it.substring(index - 5)
            listNameDel.add(text.substringBefore('?'))
           }
        return listNameDel
    }

    suspend fun listPhotoForUpload(arrayPhoto: MutableList<Uri>) : ArrayList<Uri>  = withContext(Dispatchers.IO){
        val list = arrayPhoto.filter{it.toString().contains("content")}
        return@withContext list as ArrayList<Uri>
    }
}