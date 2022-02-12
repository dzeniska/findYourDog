package com.dzenis_ska.findyourdog.ui.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.ui.MainActivity
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


object CheckNetwork {
    fun check(act: MainActivity): Boolean {
        val cManager = act.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = cManager.getNetworkCapabilities(cManager.activeNetwork)
        if (info == null) {
            Toast.makeText(act, act.resources.getString(R.string.no_network_q), Toast.LENGTH_SHORT).show()
            return false
        } else {
            if (info.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Toast.makeText(act, "Network available", Toast.LENGTH_SHORT).show()
                return true
            } else if(info.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
                Toast.makeText(act, "WIFI available", Toast.LENGTH_SHORT).show()
                return true
            } else {
                Toast.makeText(act, "NO Network!!!", Toast.LENGTH_SHORT).show()
                return false
            }
        }
    }
}



