package com.dzenis_ska.findyourdog.ui.utils

import android.app.AlertDialog
import android.icu.util.Calendar
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import com.dzenis_ska.findyourdog.databinding.CalendarDialogBinding
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.ui.fragments.AddShelterFragment

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
            Toast.makeText(act, "Сохранено!", Toast.LENGTH_LONG).show()
            Log.d("!!!DC", "${calendar.timeInMillis}")
            aSF.vaccineDataEdit(vaccine, calendar.timeInMillis,_year,
                _month,
                _dayOfMonth)
//            dialog.dismiss()
        }

    }
    const val PLAGUE = "plague"
    const val RABIES = "rabies"
}