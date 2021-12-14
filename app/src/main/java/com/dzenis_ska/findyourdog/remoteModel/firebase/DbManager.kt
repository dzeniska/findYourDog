package com.dzenis_ska.findyourdog.remoteModel.firebase


import android.net.Uri
import android.util.Log
import com.dzenis_ska.findyourdog.ui.utils.FilterManager
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class DbManager() {
    val db = Firebase.database.getReference(MAIN_NODE)
    val auth = Firebase.auth
    val ref = Firebase.storage.getReference(STORAGE_NODE)


    fun publishAdShelter(adTemp: AdShelter, callback: (text: String) -> Unit) {
        if (auth.uid != null) {
            db.child(adTemp.key ?: "empty").child(auth.uid!!).child(AD_SHELTER_NODE)
                .setValue(adTemp)
                .addOnCompleteListener { task1 ->
                    if (task1.isSuccessful) {
                        db.child(adTemp.key ?: "empty")
                            .child(VACCINE_NODE)
                            .setValue(FilterManager.createFilterVaccine(adTemp))
                            .addOnCompleteListener { task2 ->
                                if (task2.isSuccessful) {
                                    db.child(adTemp.key ?: "empty")
                                        .child(FILTER_NODE)
                                        .setValue(FilterManager.createFilter(adTemp))
                                        .addOnCompleteListener {
                                            Log.d("!!!publishAdShelterDBtask", "${adTemp}")

                                            callback("task")
                                        }
                                }
                            }
                    }
                }
        }
    }
    fun deletePhoto(url: String, listener: OnSuccessListener<Void>) {
        val desertRef = ref
            .child(auth.uid!!)
            .child(url)
        desertRef.delete().addOnSuccessListener(listener).addOnFailureListener {
            Log.d("!!!deletePhotoExeption", "${it}")
        }
    }

    fun addPhotoToStorage(adTemp: ByteArray, listener: OnCompleteListener<Uri>) {
        Log.d("!!!itTaskJopa", "${adTemp}")
        val imStorageRef = ref
            .child(auth.uid!!)
            .child("image_${System.currentTimeMillis()}")

        val upTask = imStorageRef.putBytes(adTemp)
        upTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imStorageRef.downloadUrl
        }.addOnCompleteListener(listener)
    }

    fun getAllAds(readDataCallback: ReadDataCallback?) {
        val query = db.orderByChild("/adFilterVaccine/plague")
//            .startAt("1635430194000")
        readDataFromDB(query, readDataCallback)
    }

    //на будущее pagination
    fun getAllAdsForAdapter(lng: Double, readDataCallback: ReadDataCallback?) {
        Log.d("!!!lat_lng", "${lng.minus(0.5)}_${lng.plus(0.5)}")
        val query = db.orderByChild("/adFilter/lng")
            .startAt("${lng.minus(1.0)}").endBefore("${lng.plus(1.0)}")
//            .limitToFirst(2)
        readDataFromDB(query, readDataCallback)
    }

    fun deleteAdShelter(adShelter: AdShelter?, listener: FinishWorkListener) {
        if (adShelter?.key == null || adShelter.uid == null) return
        db.child(adShelter.key).child(adShelter.uid).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) listener.onFinish()
        }
        db.child(adShelter.key).child(INFO_NODE).removeValue()
        db.child(adShelter.key).child(CALLS_NODE).removeValue()
//        придумать логику загрузки
    }

    fun adViewed(adShelter: AdShelter, anyCounter: Int, listener: FinishWorkListener) {
        val counterV = adShelter.viewsCounter.toInt()
        val counterC = adShelter.callsCounter.toInt()
        when (anyCounter) {
            BreedViewModel.VIEWS_COUNTER -> {
                if (auth.uid != null)
                    Log.d("!!!counterVC", "${counterV} ${counterC}")
                db.child(adShelter.key ?: "empty").child(INFO_NODE)
                    .setValue(InfoItem(counterV.plus(1).toString(), counterC.toString()))
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) listener.onFinish()
                    }
            }
            BreedViewModel.CALLS_COUNTER -> {
                if (auth.uid != null)
                    Log.d("!!!counterCV", "${counterV} ${counterC}")
                db.child(adShelter.key ?: "empty").child(INFO_NODE)
                    .setValue(InfoItem(counterV.toString(), counterC.plus(1).toString()))
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) listener.onFinish()
                    }
            }
        }
    }

    private fun readDataFromDB(query: Query, readDataCallback: ReadDataCallback?) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adShelterArray = ArrayList<AdShelter>()
                for (item in snapshot.children) {
                    var adShelter: AdShelter? = null
                    item.children.forEach { data ->
                        if (adShelter == null) adShelter =
                            data.child(AD_SHELTER_NODE).getValue(AdShelter::class.java)
                    }


                    val infoItem = item.child(INFO_NODE).getValue(InfoItem::class.java)
                    val isFav = auth.uid?.let { item.child(FAVS_NODE).child(it).getValue(String::class.java) }
                    adShelter?.isFav = isFav != null
                    val favCounter = item.child(FAVS_NODE).childrenCount
                    adShelter?.viewsCounter = infoItem?.viewsCounter ?: "0"
                    adShelter?.callsCounter = infoItem?.callsCounter ?: "0"
                    adShelter?.favCounter = favCounter.toString()

                    if (adShelter != null) adShelterArray.add(adShelter!!)
                }

                readDataCallback?.readData(adShelterArray)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun onFavClicked(dog: AdShelter, callback: (isFav: Boolean) -> Unit) {
        if(dog.isFav){
            removeFromFavs(dog){
                callback(it)
            }
        } else {
            addToFavs(dog){
                callback(it)
            }
        }
    }
    private fun removeFromFavs(dog: AdShelter, callback: (isFav: Boolean) -> Unit){
        dog.key?.let {key ->
            auth.uid?.let { uid ->
                db.child(key).child(FAVS_NODE).child(uid).removeValue()
                    .addOnCompleteListener { task->
                        if(task.isSuccessful) callback(false)
                    }
            }
        }
    }
    private fun addToFavs(dog: AdShelter, callback: (isFav: Boolean) -> Unit){
        dog.key?.let {key ->
            auth.uid?.let { uid ->
                db.child(key).child(FAVS_NODE).child(uid).setValue(uid)
                    .addOnCompleteListener { task->
                        if(task.isSuccessful) callback(true)
                    }
            }
        }
    }


    interface ReadDataCallback {
        fun readData(list: ArrayList<AdShelter>)

    }

    interface FinishWorkListener {
        fun onFinish()
    }

    companion object {
        const val AD_SHELTER_NODE = "adShelter"
        const val FILTER_NODE = "adFilter"
        const val VACCINE_NODE = "adFilterVaccine"
        const val INFO_NODE = "info"
        const val FAVS_NODE = "favs"
        const val CALLS_NODE = "calls"
        const val MAIN_NODE = "main"
        const val STORAGE_NODE = "storage"
        const val ADS_LIMIT = 2

    }
}