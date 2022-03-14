package com.dzenis_ska.findyourdog.remoteModel.firebase


import android.net.Uri
import android.util.Log
import com.dzenis_ska.findyourdog.ui.utils.FilterManager
import com.dzenis_ska.findyourdog.viewModel.BreedViewModel
import com.google.android.gms.tasks.OnCompleteListener
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
    val dbMap = Firebase.database.getReference(MAIN_MAP_NODE)
    val mAuth = Firebase.auth
    private val ref = Firebase.storage.getReference(STORAGE_NODE)


    fun publishAdShelter(adTemp: AdShelter, callback: (text: String) -> Unit) {
        if (mAuth.uid != null) {
            val childFirst = "${adTemp.email?.substringBefore('.')}_${adTemp.key}"
            db.child(childFirst)
                .child(mAuth.uid!!).child(AD_SHELTER_NODE)
                .setValue(adTemp)
                .addOnCompleteListener { task1 ->
                    if (task1.isSuccessful) {
                        db.child(childFirst)
                            .child(VACCINE_NODE)
                            .setValue(FilterManager.createFilterVaccine(adTemp))
                            .addOnCompleteListener { task2 ->
                                if (task2.isSuccessful) {
                                    db.child(childFirst)
                                        .child(FILTER_NODE)
                                        .setValue(FilterManager.createFilter(adTemp))
                                        .addOnCompleteListener {
                                            Log.d("!!!publishAdShelterDBtask", "${adTemp}")
                                            dbMap.child(childFirst)
                                                .setValue(
                                                    AdForMap(
                                                        name = adTemp.name,
                                                        gender = adTemp.gender,
                                                        lat = adTemp.lat,
                                                        lng = adTemp.lng,
                                                        markerColor = adTemp.markerColor,
                                                        key = adTemp.key,
                                                        uid = adTemp.uid,
                                                        email = adTemp.email
                                                    )
                                                ).addOnCompleteListener {
                                                    callback("task")
                                                }

                                        }
                                }
                            }
                    }
                }
        }
    }

    fun deletePhoto(url: String) {

        ref.storage
            .getReferenceFromUrl(url)
            .delete()
            .addOnSuccessListener {
                Log.d("!!!deletePhotoUri", " db man ${url}")
            }
    }


    fun addPhotoToStorage(adTemp: ByteArray, key: String?, listener: OnCompleteListener<Uri>) {
        Log.d("!!!itTaskJopa", "${adTemp}")
        val imStorageRef = ref
            .child(mAuth.currentUser?.email + "_" + mAuth.uid!!)
            .child(key ?: "key")
            .child("${System.currentTimeMillis().toString().substring(5)}_image")


        val upTask = imStorageRef.putBytes(adTemp)
        upTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            } else {
                imStorageRef.downloadUrl
            }
        }.addOnCompleteListener(listener)
    }

    fun getAllMarkersForMap(callback: (adShelterArray: ArrayList<AdForMap>) -> Unit) {
        dbMap.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adShelterArray = ArrayList<AdForMap>()
                snapshot.children.forEach { item ->
                    item.getValue(AdForMap::class.java)?.let { adShelterArray.add(it) }
                }
                callback(adShelterArray)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("!!!getAllAdsForMap", error.message)
            }

        })
    }

    fun getAllAds(callback: (adShelterArray: ArrayList<AdShelter>) -> Unit) {
        val query = db.orderByChild("/${FILTER_NODE}/time")
//            .startAt("1635430194000")
            .limitToFirst(100)
        readDataFromDB(query) { callback(it) }
    }

    //на будущее pagination
    fun getAllAdsForAdapter(
        lat: Double,
        lng: Double,
        isMyMarkers: Boolean,
        callback: (adShelterArray: ArrayList<AdShelter>) -> Unit
    ) {
        if (isMyMarkers) {
            val myUid = mAuth.currentUser?.uid
            val queryMyAds = db.orderByChild("/${FILTER_NODE}/uid").equalTo("$myUid")
            readDataFromDB(queryMyAds) {
                Log.d("!!!getAllAdsForAdapterMy", "${it}")

                callback(it)
            }
        } else {
            val queryLat: Query = when (lat) {
                in LOCATION_LIMIT..85.0 -> db.orderByChild("/${FILTER_NODE}/lat")
                    .startAt("${lat.minus(LOCATION_LIMIT)}")
                    .endBefore("${lat.plus(LOCATION_LIMIT)}")
                //            .limitToFirst(100)
                in -85.0..-LOCATION_LIMIT -> db.orderByChild("/${FILTER_NODE}/lat")
                    .startAt("${lat.plus(LOCATION_LIMIT)}")
                    .endBefore("${lat.minus(LOCATION_LIMIT)}")

                else -> db.orderByChild("/${FILTER_NODE}/lat").equalTo("$lat")
            }

            readDataFromDB(queryLat) { adShLat ->
                val queryLng: Query = when (lng) {
                    in LOCATION_LIMIT..179.0 -> db.orderByChild("/${FILTER_NODE}/lng")
                        .startAt("${lng.minus(LOCATION_LIMIT)}")
                        .endBefore("${lng.plus(LOCATION_LIMIT)}")
                    in -179.0..-LOCATION_LIMIT -> db.orderByChild("/${FILTER_NODE}/lng")
                        .startAt("${lng.plus(LOCATION_LIMIT)}")
                        .endBefore("${lng.minus(LOCATION_LIMIT)}")

                    else -> db.orderByChild("/${FILTER_NODE}/lng").equalTo("$lng")
                }
//            Log.d("!!!getAdsForAdapter", "${lng.minus(LOCATION_LIMIT)}_ ${lng.plus(LOCATION_LIMIT)}")
                readDataFromDB(queryLng) { adShLng ->
                    val listSh = arrayListOf<AdShelter>()
                    adShLat.forEach { adShLatItem ->
                        adShLng.forEach adShLngItem@{ adShLngItem ->
                            Log.d("!!!getAdsForAdapter", "${adShLngItem}")
                            if (adShLngItem == adShLatItem) {
                                listSh.add(adShLngItem)
                                return@adShLngItem
                            }
                        }
                    }
                    callback(listSh)
                }

            }
        }
    }

    fun deleteAdShelter(adShelter: AdShelter?, callback: (deleted: String) -> Unit) {
        if (adShelter?.key == null || adShelter.uid == null) return
        val childFirst = "${adShelter.email?.substringBefore('.')}_${adShelter.key}"
        dbMap.child(childFirst).removeValue().addOnSuccessListener {
            db.child(childFirst).child(adShelter.uid).removeValue().addOnSuccessListener {
                db.child(childFirst).child(INFO_NODE).removeValue().addOnSuccessListener {
                    db.child(childFirst).child(FAVORS_NODE).removeValue().addOnSuccessListener {
                        db.child(childFirst).child(FILTER_NODE).removeValue()
                            .addOnSuccessListener {
                                db.child(childFirst).child(VACCINE_NODE).removeValue()
                                    .addOnSuccessListener {
                                        Log.d("!!!deleted", "ok")
                                        callback("ok")
                                    }
                            }
                    }
                }
            }
        }
    }

    fun adViewed(adShelter: AdShelter, anyCounter: Int, listener: FinishWorkListener) {
        val counterV = adShelter.viewsCounter.toInt()
        val counterC = adShelter.callsCounter.toInt()
        val childFirst = "${adShelter.email?.substringBefore('.')}_${adShelter.key}"
        when (anyCounter) {
            BreedViewModel.VIEWS_COUNTER -> {
                if (mAuth.uid != null)
                    Log.d("!!!counterVC", "${counterV} ${counterC}")
                db.child(childFirst).child(INFO_NODE)
                    .setValue(InfoItem(counterV.plus(1).toString(), counterC.toString()))
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) listener.onFinish()
                    }
            }
            BreedViewModel.CALLS_COUNTER -> {
                if (mAuth.uid != null)
                    Log.d("!!!counterCV", "${counterV} ${counterC}")
                db.child(childFirst).child(INFO_NODE)
                    .setValue(InfoItem(counterV.toString(), counterC.plus(1).toString()))
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) listener.onFinish()
                    }
            }
        }
    }

    private fun readDataFromDB(
        query: Query,
        callback: (adShelterArray: ArrayList<AdShelter>) -> Unit
    ) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adShelterArray = ArrayList<AdShelter>()
                for (item in snapshot.children) {
                    var adShelter: AdShelter? = null

                    item.children.forEach { data ->
                        Log.d("!!!readDataFromDB", "${data}")
                        if (adShelter == null) adShelter =
                            data.child(AD_SHELTER_NODE).getValue(AdShelter::class.java)
                    }


                    val infoItem = item.child(INFO_NODE).getValue(InfoItem::class.java)
                    val isFav = mAuth.uid?.let {
                        item.child(FAVORS_NODE).child(it).getValue(String::class.java)
                    }
                    adShelter?.isFav = isFav != null
                    val favCounter = item.child(FAVORS_NODE).childrenCount
                    adShelter?.viewsCounter = infoItem?.viewsCounter ?: "0"
                    adShelter?.callsCounter = infoItem?.callsCounter ?: "0"
                    adShelter?.favCounter = favCounter.toString()

                    if (adShelter != null) adShelterArray.add(adShelter!!)
                }

                callback(adShelterArray)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun onFavClicked(dog: AdShelter, callback: (isFav: Boolean) -> Unit) {
        if (dog.isFav) {
            removeFromFavs(dog) {
                callback(it)
            }
        } else {
            addToFavs(dog) {
                callback(it)
            }
        }
    }

    private fun removeFromFavs(dog: AdShelter, callback: (isFav: Boolean) -> Unit) {
        val childFirst = "${dog.email?.substringBefore('.')}_${dog.key}"
        childFirst.let { key ->
            mAuth.uid?.let { uid ->
                db.child(key).child(FAVORS_NODE).child(uid).removeValue()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) callback(false)
                    }
            }
        }
    }

    private fun addToFavs(dog: AdShelter, callback: (isFav: Boolean) -> Unit) {
        val childFirst = "${dog.email?.substringBefore('.')}_${dog.key}"
        childFirst.let { key ->
            mAuth.uid?.let { uid ->
                db.child(key).child(FAVORS_NODE).child(uid).setValue(uid)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) callback(true)
                    }
            }
        }
    }

    interface FinishWorkListener {
        fun onFinish()
    }

    companion object {
        const val AD_SHELTER_NODE = "ad_shelter"
        const val FILTER_NODE = "ad_filter"
        const val VACCINE_NODE = "ad_filter_vaccine"
        const val INFO_NODE = "info"
        const val FAVORS_NODE = "favors"
        const val MAIN_NODE = "main"
        const val MAIN_MAP_NODE = "main_map"
        const val STORAGE_NODE = "storage"
        const val LOCATION_LIMIT = 10.0
    }
}


//rules_version = '2';
//service firebase.storage {
//    match /b/{bucket}/o {
//        match /{allPaths=**} {
//            allow read, write, delete: if request.auth != null;
//        }
//    }
//}
