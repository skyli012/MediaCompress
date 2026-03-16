package com.hailong.mediacompress.processor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageProcessor {

    /**
     * 压缩图片
     * @param context 上下文
     * @param inputUriString 输入图片URI字符串
     * @param outputDir 输出目录
     * @param quality 压缩质量 (0-100)
     * @param maxWidth 最大宽度，用于分辨率压缩
     * @param maxHeight 最大高度，用于分辨率压缩
     * @param format 压缩格式 (JPEG, WEBP)
     * @return 压缩后的文件
     */
    fun compressImage(
        context: Context,
        inputUri: android.net.Uri,
        outputFile: File,
        quality: Int = 80,
        maxWidth: Int = 1920,
        maxHeight: Int = 1080,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    ): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(inputUri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // 计算采样率 (Resolution compression)
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            options.inJustDecodeBounds = false

            val inputStream2: InputStream? = context.contentResolver.openInputStream(inputUri)
            var bitmap = BitmapFactory.decodeStream(inputStream2, null, options)
            inputStream2?.close()

            if (bitmap == null) return null

            // 处理图片旋转
            val inputStream3: InputStream? = context.contentResolver.openInputStream(inputUri)
            val rotation = inputStream3?.let { getRotation(it) } ?: 0
            inputStream3?.close()
            
            if (rotation != 0) {
                bitmap = rotateBitmap(bitmap, rotation)
            }

            // 质量压缩并保存
            val fos = FileOutputStream(outputFile)
            bitmap.compress(format, quality, fos)
            fos.flush()
            fos.close()
            bitmap.recycle()

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun getRotation(inputStream: InputStream): Int {
        return try {
            val exif = ExifInterface(inputStream)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
