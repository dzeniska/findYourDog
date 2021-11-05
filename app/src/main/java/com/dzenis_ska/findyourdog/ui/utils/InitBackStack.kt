package com.dzenis_ska.findyourdog.ui.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.navigation.NavController


object InitBackStack {

    @SuppressLint("RestrictedApi")
    fun initBackStack(navController: NavController){
        val fList = navController.backQueue
        fList.forEach {
            Log.d("!!!frFF", "${it.destination.label}")
        }
    }
}