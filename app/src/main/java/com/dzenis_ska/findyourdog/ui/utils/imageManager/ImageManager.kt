package com.dzenis_ska.findyourdog.ui.utils.imageManager

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

object ImageManager {

    private const val MAX_IMAGE_SIZE = 1000
    private const val WIDTH = 0
    private const val HEIGHT = 1

    fun getImageSize(uri: Uri, act: Activity): List<Int>?{
        val inStream = try{
            act.contentResolver.openInputStream(uri)
        }catch (e: Exception){
            null
        }
        return if(inStream != null){
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inStream, null, options)
            listOf(options.outWidth, options.outHeight)
        }else{
            null
        }
    }
    suspend fun fromStorageToByteArray(photoes:String, activity: Activity) = withContext(Dispatchers.IO) {

            val uri = toUri(photoes, activity)

        return@withContext uri
    }

    suspend fun toUri(uri: String, act: Activity): Uri = withContext(Dispatchers.IO) {

        val bitMap = Picasso.get()
            .load(uri)
            .get()

        val uriF = File(act.cacheDir, "temp.tmp")
            uriF.writeBitmap(bitMap, Bitmap.CompressFormat.JPEG, 100)


//            val fTemp = File(act.cacheDir, "temp.tmp")
//            val inputStream: InputStream
//            inputStream = uri.inputStream()
//            inputStream.saveToFile(fTemp)
            val uriP = Uri.fromFile(uriF)

        return@withContext uriP
    }
    private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
        outputStream().use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }

    /*fun getImageShize(uri: Uri, act: Activity): List<Int>{
        val inStream = act.contentResolver.openInputStream(uri)
        val fTemp = File(act.cacheDir, "temp.tmp")
        if (inStream != null) {
            fTemp.copyInStreamToFile(inStream)
        }
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(fTemp.path, options)
        return if (imageRotation( fTemp) == 90) {
            listOf(options.outHeight, options.outWidth)
        } else {
            listOf(options.outWidth, options.outHeight)
        }
    }*/

    private fun File.copyInStreamToFile(inStream: InputStream) {
        this.outputStream().use {out ->
            inStream.copyTo(out)
        }
    }
    fun imageRotation(imageFile: File): Int{

//        val imageFIle = File(uri.toString())
        val exif = ExifInterface(imageFile.absolutePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return if (orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            90
        } else {
            0
        }
    }
    suspend fun imageRotationPreview(uri: Uri, act: Activity): Int = withContext(Dispatchers.IO){
        val inStream = try{
            act.contentResolver.openInputStream(uri)
        }catch (e: Exception){
            null
        }
        val imageFIle = File(act.cacheDir,"temp.tmp")
        var orientation = 0
        if (inStream != null) {
            imageFIle.copyInStreamToFile(inStream)

            val exif = ExifInterface(imageFIle.absolutePath)
            orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }
        return@withContext if (orientation == ExifInterface.ORIENTATION_ROTATE_90){
            90
        }else if (orientation == ExifInterface.ORIENTATION_ROTATE_270){
            270
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180){
            180
        }else{
            0
        }
    }
    fun imageRotationNew(uri: Uri, act: Activity): Int{
        val inStream = act.contentResolver.openInputStream(uri)
        val exif = inStream?.let { ExifInterface(it) }
        val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)


        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            else -> 0
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

    suspend fun imageResize(uris: List<Uri>, act: Activity): ArrayList<ByteArray> = withContext(Dispatchers.IO) {
        val tempList = ArrayList<List<Int>>()
        val bitmapList = ArrayList<Bitmap>()
        for (n in uris.indices) {
            val size = getImageSize(uris[n], act)
            val rotationDegres = imageRotationNew(uris[n], act)
            //проверка на каличное фото
            Log.d("!!!or", "${size}, ${rotationDegres}")
            if(size != null){
                val imageRatio = size[WIDTH].toDouble() / size[HEIGHT].toDouble()

                    if (imageRatio > 1) {
//                        if(rotationDegres == 0 || rotationDegres == 180) {
                            if (size[WIDTH] > MAX_IMAGE_SIZE) {
                                tempList.add(
                                    listOf(
                                        MAX_IMAGE_SIZE,
                                        (MAX_IMAGE_SIZE / imageRatio).toInt()
                                    )
                                )
                            } else {
                                tempList.add(listOf(size[WIDTH], size[HEIGHT]))
                            }
//                        }
                    } else {
                        if (size[HEIGHT] > MAX_IMAGE_SIZE) {
                            tempList.add(listOf((MAX_IMAGE_SIZE * imageRatio).toInt(), MAX_IMAGE_SIZE))
                        } else {
                            tempList.add(listOf(size[WIDTH], size[HEIGHT]))
                        }
                    }


            }else{
                return@withContext  arrayListOf<ByteArray>()
            }

        }

        for (i in uris.indices) {
//            val e = kotlin.runCatching {

            val rotationDegres = imageRotationNew(uris[i], act)
            bitmapList.add(Picasso.get()
                .load(uris[i])
                .resize(tempList[i][WIDTH], tempList[i][HEIGHT])
                .rotate(rotationDegres.toFloat())
                .get()
            )
//            }
//            Log.d("!!!", "$e")
        }
        val bmlA = arrayListOf<ByteArray>()
        bitmapList.forEach { bitmap ->
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25,out)
            val biteArray = out.toByteArray()
            bmlA.add(biteArray)
        }
        return@withContext bmlA
    }
}