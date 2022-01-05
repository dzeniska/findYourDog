package com.dzenis_ska.findyourdog.ui.utils

import android.app.AlertDialog
import android.icu.util.Calendar
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import com.dzenis_ska.findyourdog.databinding.CalendarDialogBinding
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.ui.fragments.AddShelterFragment
import java.text.SimpleDateFormat
import java.util.*

object DialogCalendar {
    private var _year: Int = 0
    private var _month: Int = 0
    private var _dayOfMonth: Int = 0
    fun createDialogCalendar(
        act: MainActivity,
        aSF: AddShelterFragment,
        vaccine: String,
        time: String?,
        isSave: Boolean?
    ){
        val builder = AlertDialog.Builder(act)
        val rootDialogElement = CalendarDialogBinding.inflate(act.layoutInflater)
        val view = rootDialogElement.root
        builder.setView(view)
        val dialog = builder.create()
        dialog.show()

        if(time != null && time != "null"){
            rootDialogElement.calendarView.date = time.toLong()
        }
           if(isSave == false) rootDialogElement.btnSave.isVisible = false



        rootDialogElement.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            rootDialogElement.btnSave.isVisible = true
            _year = year
            _month = month
            _dayOfMonth = dayOfMonth
        }
        rootDialogElement.btnExit.setOnClickListener {
            dialog.dismiss()
        }
        rootDialogElement.btnSave.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.set(
                _year,
                _month,
                _dayOfMonth
            )
//            rootDialogElement.calendarView.date = calendar.timeInMillis
//            Toast.makeText(act, "Сохранено!", Toast.LENGTH_LONG).show()
            val timeMillis = getCurrentData()
            Log.d("!!!DC", "${timeMillis}")
            val calTimeMillis = calendar.timeInMillis
            Log.d("!!!DC", "${calTimeMillis}")

//            if(calTimeMillis < 0){
//                val s = SimpleDateFormat("yyyy_MM_dd")
//                val format: String = s.format(Date())
//                Log.d("!!!DC", "${format}")
//                _year = format.split("_")[0].toInt()
//                _month = format.split("_")[1].toInt().minus(1)
//                _dayOfMonth = format.split("_")[2].toInt()
//                calendar.set(
//                    _year,
//                    _month,
//                    _dayOfMonth
//                )
//                val calTimeMillis = calendar.timeInMillis
//
//                aSF.vaccineDataEdit(vaccine, calTimeMillis,_year,
//                    _month,
//                    _dayOfMonth)
//            } else {
                aSF.vaccineDataEdit(vaccine, calTimeMillis,_year,
                    _month,
                    _dayOfMonth)
//            }
            dialog.dismiss()
        }

    }
    private fun getCurrentData() = System.currentTimeMillis()
    const val PLAGUE = "plague"
    const val RABIES = "rabies"
}