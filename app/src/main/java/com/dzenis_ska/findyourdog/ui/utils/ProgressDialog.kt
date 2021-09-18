package com.dzenis_ska.findyourdog.ui.utils

import android.app.Activity
import android.app.AlertDialog
import android.view.View
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.databinding.ProgressDialogLayoutBinding

object ProgressDialog {
    const val MAIN_ACTIVITY = 0
    const val ADD_SHELTER_FRAGMENT = 1
    fun createProgressDialog(act: Activity, const: Int): AlertDialog{
        val builder = AlertDialog.Builder(act)
        val rootDialogElement = ProgressDialogLayoutBinding.inflate(act.layoutInflater)
        val view = rootDialogElement.root
        builder.setView(view)
        val dialog = builder.create()
        showElements(const, rootDialogElement, act, dialog)
        return dialog
    }

    private fun showElements(
        const: Int,
        rootDialogElement: ProgressDialogLayoutBinding,
        act: Activity,
        dialog: AlertDialog
    ) {
        rootDialogElement.apply {
            when(const){
                ADD_SHELTER_FRAGMENT -> {
                    dialog.setCancelable(false)
                }
                MAIN_ACTIVITY -> {
                    progressBar2.visibility = View.GONE
                    textView.text = act.resources.getString(R.string.no_network_q)
                    dialog.setCancelable(true)
                }
            }
        }
    }
}