package com.dzenis_ska.findyourdog.ui.utils.imageManager

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.github.dhaval2404.imagepicker.ImagePicker


class ImagePickerManager(
    registry: ActivityResultRegistry,
    private val owner: LifecycleOwner,
    val callback: (imageUri: Uri?, callbackResult: String?) -> Unit
) {

    private val getContent: ActivityResultLauncher<Intent> =
        registry.register(RESULT_REGISTRY_KEY, owner, ActivityResultContracts.StartActivityForResult()){ result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            if (resultCode == Activity.RESULT_OK) {
                //Image Uri will not be null for RESULT_OK
                val fileUri = data?.data!!
                callback(fileUri, null)
            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                callback(null, ImagePicker.getError(data))
            } else {
                callback(null, "Task Cancelled")
            }
        }

    fun pickImage(fragment: Fragment) {
        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.DESTROYED)) {
            Log.w(TAG, "Linked LifecycleOwner is destroyed")
            return
        }
        ImagePicker.with(fragment)
            .compress(1024)         //Final image size will be less than 1 MB(Optional)
            .maxResultSize(1080, 1080)  //Final image resolution will be less than 1080 x 1080(Optional)
            .createIntent { intent ->
                getContent.launch(intent)
            }
    }

    companion object {
        const val TAG = "ImagePicker"
        const val RESULT_REGISTRY_KEY = "pick_image"
    }
}
