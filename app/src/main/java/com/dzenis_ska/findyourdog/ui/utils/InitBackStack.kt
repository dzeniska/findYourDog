package com.dzenis_ska.findyourdog.ui.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.dzenis_ska.findyourdog.R

object InitBackStack {

    @SuppressLint("RestrictedApi")
    fun initBackStack(navController: NavController){
        val fList = navController.backStack
        fList.forEach {
            Log.d("!!!frFF", "${it.destination.label}")

        }
    }

}