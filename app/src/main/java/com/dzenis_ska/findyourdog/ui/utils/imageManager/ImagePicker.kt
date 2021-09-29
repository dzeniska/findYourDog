package com.dzenis_ska.findyourdog.ui.utils.imageManager

import android.net.Uri
import android.util.Log
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

    fun choosePhotoes(
        mAct: MainActivity,
        addSF: AddShelterFragment,
        imageCount: Int,
        const: Int
    ) {
        addSF.viewModel.backPressed = 2
        mAct.addPixToActivity(R.id.clMain, getOptions(imageCount)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    addSF.viewModel.backPressed = 0
                    val listMap = mutableMapOf<Boolean, Uri>()
                    result.data.forEach {
                        Log.d("!!!result", "showCameraFragment: ${it.path}")
                        listMap.set(false, it)
                    }

                    when (const) {
                        AddShelterFragment.ADD_PHOTO -> addSF.vpAdapter.updateAdapter(result.data, false)
                        AddShelterFragment.ADD_IMAGE -> addSF.vpAdapter.updateAdapterForSinglePhoto(result.data)
                        AddShelterFragment.REPLACE_IMAGE -> addSF.vpAdapter.replaceItemAdapter(result.data)
                    }

                    addSF.hideAddShelterButton(true)
                    closePixFragment(mAct)
                }
                PixEventCallback.Status.BACK_PRESSED -> {

                    Log.d("!!!PixBackPressed", "Pix")
                    //Не работает
//                    mAct.onBackPressed()

//                    addSF.hideAddShelterButton(true, 1)
//                    closePixFragment(mAct)
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
