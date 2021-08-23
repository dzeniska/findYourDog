package com.dzenis_ska.findyourdog.remoteModel.firebase


import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DbManager() {
    val db = Firebase.database.getReference(MAIN_NODE)
    val auth = Firebase.auth
    val ref = Firebase.storage.getReference(STORAGE_NODE)


    fun publishAdShelter(adTemp: AdShelter, callback: (text: String)-> Unit) {
        if (auth.uid != null) {
            db.child(adTemp.key ?: "empty").child(auth.uid!!).child("adShelter").setValue(adTemp)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("!!!publishAdShelterDBtask", "${adTemp}")
                        callback("task")
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

            val imStorageRef = ref
                .child(auth.uid!!)
                .child("image_${System.currentTimeMillis()}")

            val upTask = imStorageRef.putBytes(adTemp)
            upTask.continueWithTask{task->
//                Log.d("!!!itTaskSuccessful3", "${task.result}")
                imStorageRef.downloadUrl
            }.addOnCompleteListener(listener)
    }

    fun getAllAds(readDataCallback: ReadDataCallback?) {
        val query = db.orderByChild(auth.uid + "/adShelter/tel")
        readDataFromDB(query, readDataCallback)
    }

    fun deleteAdShelter(adShelter: AdShelter?, listener: FinishWorkListener) {
        if (adShelter?.key == null || adShelter.uid == null) return
        db.child(adShelter.key).child(adShelter.uid).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) listener.onFinish()
        }
        db.child(adShelter.key).child(INFO_NODE).removeValue()
//        придумать логику загрузки
    }

    fun adViewed(adShelter: AdShelter, listener: FinishWorkListener) {
        var counter = adShelter.viewsCounter.toInt()
        counter++
        Log.d("!!!views", "${auth.uid}")
//        сделать не нулл
        if (auth.uid != null)
            db.child(adShelter.key ?: "empty").child(INFO_NODE)
                .setValue(InfoItem(counter.toString()))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) listener.onFinish()
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
//                    adShelter = item.children.iterator().next().child("adShelter").getValue(AdShelter::class.java)
                    adShelter?.viewsCounter = infoItem?.viewsCounter ?: "0"
                    if (adShelter != null) adShelterArray.add(adShelter!!)
                }

                readDataCallback?.readData(adShelterArray)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    interface ReadDataCallback {
        fun readData(list: ArrayList<AdShelter>)

    }

    interface FinishWorkListener {
        fun onFinish()
    }

    companion object {
        const val AD_SHELTER_NODE = "adShelter"
        const val INFO_NODE = "info"
        const val MAIN_NODE = "main"
        const val STORAGE_NODE = "storage"
    }
}