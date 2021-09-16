package com.dzenis_ska.findyourdog.ui.utils.imageManager

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.dzenis_ska.findyourdog.R
import com.dzenis_ska.findyourdog.ui.MainActivity
import com.dzenis_ska.findyourdog.ui.fragments.AddShelterFragment
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import io.ak1.pix.models.Ratio

object ImagePicker {


    fun getOptions(imageCount: Int): Options {
        val options = Options().apply() {
            ratio = Ratio.RATIO_AUTO
            count = imageCount
            isFrontFacing = false
            mode = Mode.Picture
            path = "/pix/images"
        }
        return options
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

                    addSF.hideAddShelterButton(true, 1)
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

    fun getLauncherForMultiSelectImages(addSF: AddShelterFragment): ActivityResultLauncher<Intent> {
        return addSF.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            /*if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null) {
                    val returnValues = result.data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                    if (returnValues?.size!! == 0) {
                        Toast.makeText(addSF.context, "No selected photo", Toast.LENGTH_LONG).show()
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
//                            val bitmapList = ImageManager.imageResize(returnValues)
//                            addSF.vpAdapter.updateAdapter(bitmapList as MutableList<Bitmap>)
                            addSF.vpAdapter.updateAdapter(returnValues)
                        }
                    }
                }
            }*/
        }
    }

    fun getLauncherForSingleSelectImages(addSF: AddShelterFragment): ActivityResultLauncher<Intent> {
        return addSF.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            /*if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null) {
                    val returnValues = result.data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                    if (returnValues?.size!! == 0) {
                        Toast.makeText(addSF.context, "No selected photo", Toast.LENGTH_LONG).show()
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
//                            val bitmapList = ImageManager.imageResize(returnValues)
//                            addSF.vpAdapter.updateAdapter(bitmapList as MutableList<Bitmap>)
                            addSF.vpAdapter.updateAdapterForSinglePhoto(returnValues)
                        }
                    }
                }
            }*/
        }
    }

    fun getLauncherForReplaceSelectedImage(addSF: AddShelterFragment): ActivityResultLauncher<Intent>? {
        return addSF.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            /* if (result.resultCode == Activity.RESULT_OK) {
                 if (result.data != null) {
                     val returnValues = result.data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)

                     CoroutineScope(Dispatchers.Main).launch {
 //                        val bitmapList = ImageManager.imageResize(returnValues as List<String>)
 //                        addSF.vpAdapter.replaceItemAdapter(bitmapList as MutableList<Bitmap>)
                         addSF.vpAdapter.replaceItemAdapter(returnValues!!)
                     }

                 }
             }*/
        }
    }
}
