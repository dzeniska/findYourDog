package com.dzenis_ska.findyourdog.remoteModel.firebase


import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DbManager() {
    val db = Firebase.database.getReference(MAIN_NODE)
    val auth = Firebase.auth

    fun publishAdShelter(adTemp: AdShelter, writeDataCallback: WriteDataCallback?) {
        if(auth.uid != null) db.child(adTemp.key?: "empty").child(auth.uid!!).child("adShelter").setValue(adTemp)
            .addOnCompleteListener { task->
            if(task.isSuccessful){
                writeDataCallback?.writeData()
            }
        }
    }

    fun getAllAds(readDataCallback: ReadDataCallback?){
        val query = db.orderByChild(auth.uid + "/adShelter/tel")
        readDataFromDB(query, readDataCallback)
    }
    fun deleteAdShelter(adShelter: AdShelter?, listener: FinishWorkListener){
        if(adShelter?.key == null || adShelter.uid == null) return
        db.child(adShelter.key).child(adShelter.uid).removeValue().addOnCompleteListener {task->
            if(task.isSuccessful) listener.onFinish()
        }
        db.child(adShelter.key).child(INFO_NODE).removeValue()
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

   private fun readDataFromDB(query: Query, readDataCallback: ReadDataCallback?){
        query.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val adShelterArray = ArrayList<AdShelter>()
                for(item in snapshot.children){
                    var adShelter: AdShelter? = null
                    item.children.forEach {data ->
                        if(adShelter == null) adShelter = data.child(AD_SHELTER_NODE).getValue(AdShelter::class.java)
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

    interface WriteDataCallback {
        fun writeData()
    }

    interface  FinishWorkListener{
        fun onFinish()
    }

    companion object{
        const val AD_SHELTER_NODE = "adShelter"
        const val INFO_NODE = "info"
        const val MAIN_NODE = "main"
        const val FAVS_NODE = "favs"
    }
}