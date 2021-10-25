package com.dzenis_ska.findyourdog.ui.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.navigation.NavController
import com.dzenis_ska.findyourdog.R

object InitBackStack {
    @SuppressLint("RestrictedApi")
    fun initBackStack(navController: NavController){
        val fList = navController.backStack
        var count = 0
        var countReg = 0
        fList.forEach {
            if(it.destination.label == "Карта"){
                count++
                if(count == 2){
                    navController.popBackStack(R.id.mapsFragment, true)
                }
            }
            if(it.destination.label == "Регистрация"){
                countReg++
                if(countReg == 2){
                    navController.popBackStack(R.id.loginFragment, true)
                }
            }
            Log.d("!!!frMF", "${it.destination.label}, count: $count")
            Log.d("!!!frMF", "${it.destination.label}, count: $countReg")
        }
    }
}