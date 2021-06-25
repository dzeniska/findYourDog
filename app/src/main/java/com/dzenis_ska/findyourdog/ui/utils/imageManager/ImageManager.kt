package com.dzenis_ska.findyourdog.ui.utils.imageManager

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.exifinterface.media.ExifInterface
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


object ImageManager {

    private const val MAX_IMAGE_SIZE = 1000
    private const val WIDTH = 0
    private const val HEIGHT = 1

    fun getImageSize(uri: String): List<Int>{
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(uri, options)
        return if (imageRotation(uri) == 90) {
            listOf(options.outHeight, options.outWidth)
        } else {
            listOf(options.outWidth, options.outHeight)
        }

    }

    fun imageRotation(uri: String): Int{
        val imageFIle = File(uri)
        val exif = ExifInterface(imageFIle.absolutePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return if (orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            90
        } else {
            0
        }
    }
    fun imageRotationPreview(uri: String): Int{
        val imageFIle = File(uri)
        val exif = ExifInterface(imageFIle.absolutePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            90
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270){
            270
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180){
            180
        }else{
            0
        }
    }

    fun chooseScaleType(im: SubsamplingScaleImageView, bitMap: Bitmap){
        if(bitMap.width > bitMap.height) {
//            im.importantForAutofill = SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP
//            im.scaleType = ImageView.ScaleType.CENTER_CROP
        }else{
//            im.autofillType = SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE
//            im.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
    }

    suspend fun imageResize(uris: List<String>): List<Bitmap> = withContext(Dispatchers.IO) {
        val tempList = ArrayList<List<Int>>()
        val bitmapList = ArrayList<Bitmap>()
        for (n in uris.indices) {
            val size = getImageSize(uris[n])
            val imageRatio = size[WIDTH].toDouble() / size[HEIGHT].toDouble()
            if (imageRatio > 1) {
                if (size[WIDTH] > MAX_IMAGE_SIZE) {
                    tempList.add(listOf(MAX_IMAGE_SIZE, (MAX_IMAGE_SIZE / imageRatio).toInt()))
                } else {
                    tempList.add(listOf(size[WIDTH], size[HEIGHT]))
                }
            } else {
                if (size[HEIGHT] > MAX_IMAGE_SIZE) {
                    tempList.add(listOf((MAX_IMAGE_SIZE * imageRatio).toInt(), MAX_IMAGE_SIZE))
                } else {
                    tempList.add(listOf(size[WIDTH], size[HEIGHT]))
                }
            }
        }
        for (i in uris.indices) {
//            val e = kotlin.runCatching {
            bitmapList.add(
                Picasso.get()
                .load(File(uris[i]))
                .resize(tempList[i][WIDTH], tempList[i][HEIGHT])
                .get()
            )
//            }
//            Log.d("!!!", "$e")
        }
        return@withContext bitmapList
    }
}