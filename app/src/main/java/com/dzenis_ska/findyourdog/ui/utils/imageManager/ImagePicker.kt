package com.dzenis_ska.findyourdog.ui.utils.imageManager

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.navigation.NavController
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.ui.fragments.AddShelterFragment
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import io.ak1.pix.models.Ratio

object ImagePicker {


    private fun getOptions(imageCount: Int): Options {
        return Options().apply() {
            ratio = Ratio.RATIO_AUTO
            count = imageCount
            isFrontFacing = false
            mode = Mode.Picture
            path = "/pix/images"
        }
    }

    @SuppressLint("RestrictedApi")
    fun choosePhotoes(
        mAct: MainActivity,
        addSF: AddShelterFragment,
        imageCount: Int,
        const: Int
    ) {
        addSF.hideAddShelterButton(false)
        addSF.viewModel.backPressed = 2
        mAct.addPixToActivity(R.id.clMain, getOptions(imageCount)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    addSF.viewModel.backPressed = 0

                    when (const) {
                        AddShelterFragment.ADD_PHOTO -> {
                            if(result.data.isNotEmpty())
                            addSF.vpAdapter.updateAdapter(result.data, false)
                            else
                                addSF.hideAddPhoto(true)
                        }
                        AddShelterFragment.ADD_IMAGE -> addSF.vpAdapter.updateAdapterForSinglePhoto(result.data)
                        AddShelterFragment.REPLACE_IMAGE -> addSF.vpAdapter.replaceItemAdapter(result.data)
                    }
                    addSF.hideAddShelterButton(true)
                    closePixFragment(mAct)
                }
                PixEventCallback.Status.BACK_PRESSED -> {
                    Log.d("!!!PixBackPressed", "Pix")
                    //Не работает
                }
            }
        }
    }

    private fun closePixFragment(mAct: MainActivity) {
        val fList = mAct.supportFragmentManager.fragments
        fList.forEach { frag ->
            if (frag.toString().startsWith("PixFragment")) {
                mAct.supportFragmentManager.beginTransaction().remove(frag).commit()
//                mAct.navController.navigate(R.id.addShelterFragment)
            }
        }
    }
}
