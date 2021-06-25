package com.dzenis_ska.findyourdog.ui.utils.imageManager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.dzenis_ska.findyourdog.ui.fragments.AddShelterFragment
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.fxn.utility.PermUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ImagePicker {


    fun getOptions(imageCount: Int): Options {
        val options = Options.init()
//            .setRequestCode(100) //Request code for activity results
            .setCount(imageCount) //Number of images to restict selection count
            .setFrontfacing(false) //Front Facing camera on start
//            .setPreSelectedUrls(returnValue) //Pre selected Image Urls
//            .setSpanCount(4) //Span count for gallery min 1 & max 5
            .setMode(Options.Mode.Picture) //Option to select only pictures or videos or both
//            .setVideoDurationLimitinSeconds(30) //Duration for video recording
            .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT) //Orientaion
            .setPath("/pix/images") //Custom Path For media Storage

        return options
//        Pix.start(this@MainActivity, options)
    }

    fun launcher(
        context: Context?,
        addSF: AddShelterFragment,
        launcher: ActivityResultLauncher<Intent>?,
        imageCount: Int,
    ) {
        PermUtil.checkForCamaraWritePermissions(addSF) {
            val intent = Intent(context, Pix::class.java).apply {
                putExtra("options", getOptions(imageCount))
            }
            launcher?.launch(intent)
        }
    }

    fun getLauncherForMultiSelectImages(addSF: AddShelterFragment): ActivityResultLauncher<Intent> {
        return addSF.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
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
            }
        }
    }
    fun getLauncherForSingleSelectImages(addSF: AddShelterFragment): ActivityResultLauncher<Intent> {
        return addSF.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null) {
                    val returnValues = result.data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)

                    CoroutineScope(Dispatchers.Main).launch {
//                        val bitmapList = ImageManager.imageResize(returnValues as List<String>)
//                        addSF.vpAdapter.replaceItemAdapter(bitmapList as MutableList<Bitmap>)
                            addSF.vpAdapter.updateAdapter(returnValues!!)
                    }

                }
            }
        }
    }
}
