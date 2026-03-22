package com.hailong.mediacompress.utils

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream

object MediaUtils {

    /**
     * 将文件保存到系统相册
     * @param context 上下文
     * @param file 要保存的文件
     * @param isVideo 是否为视频
     * @param mimeType MIME类型，如果不传则根据文件后缀推断
     * @return 保存后的Uri，如果失败则返回null
     */
    fun saveToGallery(context: Context, file: File, isVideo: Boolean, mimeType: String? = null): Uri? {
        val originalName = file.name
        val timestamp = System.currentTimeMillis()
        // 增加时间戳后缀防止重名导致保存失败
        val fileName = if (originalName.contains(".")) {
            val name = originalName.substringBeforeLast(".")
            val ext = originalName.substringAfterLast(".")
            "${name}_$timestamp.$ext"
        } else {
            "${originalName}_$timestamp"
        }

        val finalMimeType = mimeType ?: if (isVideo) {
            val ext = file.extension.toLowerCase(java.util.Locale.ROOT)
            when (ext) {
                "mp4" -> "video/mp4"
                "mov", "quicktime" -> "video/quicktime"
                "mkv" -> "video/x-matroska"
                "avi" -> "video/x-msvideo"
                else -> "video/mp4"
            }
        } else {
            val ext = file.extension.toLowerCase(java.util.Locale.ROOT)
            when (ext) {
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "webp" -> "image/webp"
                "gif" -> "image/gif"
                else -> "image/jpeg"
            }
        }

        val relativePath = if (isVideo) {
            "${Environment.DIRECTORY_MOVIES}/MediaCompress"
        } else {
            "${Environment.DIRECTORY_PICTURES}/MediaCompress"
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, finalMimeType)
            put(MediaStore.MediaColumns.DATE_ADDED, timestamp / 1000)
            put(MediaStore.MediaColumns.DATE_TAKEN, timestamp)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val collection = if (isVideo) {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentResolver = context.contentResolver
        val uri = contentResolver.insert(collection, contentValues)

        uri?.let {
            try {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    FileInputStream(file).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    contentResolver.update(it, contentValues, null, null)
                }
                
                // 即使是新版本，也调用一次 scanFile，确保小米等设备能立即在相册看到
                MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null) { path, scanUri ->
                    // 扫描完成回调
                }
                
                return it
            } catch (e: Exception) {
                e.printStackTrace()
                contentResolver.delete(it, null, null)
            }
        }
        return null
    }
}
