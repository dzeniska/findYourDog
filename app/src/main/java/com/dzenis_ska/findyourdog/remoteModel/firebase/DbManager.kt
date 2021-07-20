package com.dzenis_ska.findyourdog.remoteModel.firebase


import android.widget.Toast
import com.google.android.material.internal.ContextUtils.getActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DbManager() {
    val db = Firebase.database.getReference("main")
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

   private fun readDataFromDB(query: Query, readDataCallback: ReadDataCallback?){
        query.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val adShelterArray = ArrayList<AdShelter>()
                for(item in snapshot.children){
                    val adShelter = item.children.iterator().next().child("adShelter").getValue(AdShelter::class.java)
                    if (adShelter != null) adShelterArray.add(adShelter)
//                    Log.d("!!!readDataFromD", "${adShelter!!.lat}")
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
}