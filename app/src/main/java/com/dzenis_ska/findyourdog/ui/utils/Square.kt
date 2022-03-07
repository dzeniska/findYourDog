package com.dzenis_ska.findyourdog.ui.utils

import android.graphics.*
import com.squareup.picasso.Transformation

class CropSquareTransformation : Transformation {
    override fun transform(source: Bitmap): Bitmap {
        val size = Math.min(source.width, source.height)
        val x = (source.width - size) / 2
        val y = (source.height - size) / 2
        val result = Bitmap.createBitmap(source, x, y, size, size)
        if (result != source) {
            source.recycle()
        }
        return result
    }

     override fun key(): String {
        return "square()"
    }
}
