package com.tips.tipuous.utilities

import android.graphics.Bitmap
import android.graphics.BitmapFactory

object BitmapUtils {
    /**
     * Decodes a bitmap from a file path with downsampling.
     *
     * @param path The absolute path to the image file.
     * @param reqWidth The required width of the resulting bitmap.
     * @param reqHeight The required height of the resulting bitmap.
     * @return The downsampled bitmap, or null if decoding fails.
     */
    fun decodeSampledBitmapFromFile(
        path: String,
        reqWidth: Int,
        reqHeight: Int,
    ): Bitmap? {
        // First decode with inJustDecodeBounds=true to check dimensions
        val options =
            BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
        BitmapFactory.decodeFile(path, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return try {
            BitmapFactory.decodeFile(path, options)
        } catch (_: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int,
    ): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}
