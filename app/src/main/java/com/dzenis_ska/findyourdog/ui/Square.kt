package com.dzenis_ska.findyourdog.ui

import android.graphics.*
import com.squareup.picasso.Transformation

/*
class CircularTransformation : Transformation {
    override fun transform(source: Bitmap): Bitmap {
        val paint = Paint()
        paint.setAntiAlias(true)
        paint.setShader(BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP))
        val output =
            Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawCircle(
            (source.width / 2).toFloat(),
            (source.height / 2).toFloat(),
            (source.width / 2).toFloat(),
            paint
        )
        if (source != output) source.recycle()
        return output
    }

    override fun key(): String {
        return "circle"
    }
}*/




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
