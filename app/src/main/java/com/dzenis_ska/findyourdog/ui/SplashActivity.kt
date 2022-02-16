package com.dzenis_ska.findyourdog.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.dzenis_ska.findyourdog.databinding.ActivitySplashBinding

class SplashActivity: AppCompatActivity() {
    private lateinit var timer: CountDownTimer
    private lateinit var preferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashBinding.inflate(layoutInflater).also { setContentView(it.root) }
        setupTimer()
    }

    private fun setupTimer() {
        timer = object : CountDownTimer(isFirstLaunch() * 1000, 1000){
            override fun onTick(millisUntilFinished: Long) {
            }
            override fun onFinish() {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }

        }
    }

    private fun isFirstLaunch(): Long {
        preferences = getSharedPreferences("launch", Context.MODE_PRIVATE)
        return preferences.getLong("key", 5L).also { preferences.edit().putLong("key", 1L).apply() }
    }

    override fun onStart() {
        super.onStart()
        timer.start()
    }

    override fun onStop() {
        super.onStop()
        timer.cancel()
    }
}