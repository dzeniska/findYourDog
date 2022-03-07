package com.dzenis_ska.findyourdog.ui.fragments

import android.widget.Toast
import androidx.fragment.app.Fragment

fun Fragment.res(stringId: Int) = resources.getString(stringId)
fun Fragment.toastS(text: String) = Toast.makeText(requireActivity(), text, Toast.LENGTH_SHORT).show()
fun Fragment.toastL(text: String) = Toast.makeText(requireActivity(), text, Toast.LENGTH_SHORT).show()
fun Fragment.toastLong(stringId: Int) = Toast.makeText(requireActivity(), resources.getString(stringId), Toast.LENGTH_SHORT).show()
fun Fragment.toastShort(stringId: Int) = Toast.makeText(requireActivity(), resources.getString(stringId), Toast.LENGTH_SHORT).show()