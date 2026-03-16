package com.hailong.mediacompress.repository

import android.content.Context
import android.net.Uri
import com.hailong.mediacompress.model.CompressionStatus
import com.hailong.mediacompress.model.MediaItem
import com.hailong.mediacompress.model.MediaType
import com.hailong.mediacompress.processor.ImageProcessor
import com.hailong.mediacompress.processor.VideoProcessor
import com.hailong.mediacompress.utils.MediaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File

class MediaRepository(private val context: Context) {

    private val _compressionTasks = MutableStateFlow<List<MediaItem>>(emptyList())
    val compressionTasks: StateFlow<List<MediaItem>> = _compressionTasks

    private val outputDir: File by lazy {
        val dir = File(context.getExternalFilesDir(null), "CompressedMedia")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    suspend fun compressImage(mediaItem: MediaItem, quality: Int, maxWidth: Int, maxHeight: Int) = withContext(Dispatchers.IO) {
        updateTaskStatus(mediaItem.id, CompressionStatus.PROCESSING, 0f)
        
        val outputFile = File(outputDir, "img_${System.currentTimeMillis()}_${mediaItem.name}")
        val resultFile = ImageProcessor.compressImage(
            context,
            mediaItem.uri,
            outputFile,
            quality,
            maxWidth,
            maxHeight
        )

        if (resultFile != null) {
            // 保存到相册
            MediaUtils.saveToGallery(context, resultFile, false)
            updateTaskStatus(mediaItem.id, CompressionStatus.COMPLETED, 1f, resultFile.absolutePath, resultFile.length())
        } else {
            updateTaskStatus(mediaItem.id, CompressionStatus.FAILED, 0f)
        }
    }

    fun compressVideo(mediaItem: MediaItem, crf: Int, scale: String?) {
        updateTaskStatus(mediaItem.id, CompressionStatus.PROCESSING, 0f)
        
        val outputFile = File(outputDir, "vid_${System.currentTimeMillis()}_${mediaItem.name}")
        
        VideoProcessor.compressVideo(
            context,
            mediaItem.path,
            outputFile.absolutePath,
            crf,
            scale,
            "fast",
            object : VideoProcessor.VideoCompressListener {
                override fun onProgress(progress: Float) {
                    updateTaskStatus(mediaItem.id, CompressionStatus.PROCESSING, progress)
                }

                override fun onComplete(success: Boolean, outputPath: String?) {
                    if (success && outputPath != null) {
                        val resultFile = File(outputPath)
                        // 保存到相册
                        MediaUtils.saveToGallery(context, resultFile, true)
                        updateTaskStatus(mediaItem.id, CompressionStatus.COMPLETED, 1f, outputPath, resultFile.length())
                    } else {
                        updateTaskStatus(mediaItem.id, CompressionStatus.FAILED, 0f)
                    }
                }
            }
        )
    }

    private fun updateTaskStatus(
        id: Long, 
        status: CompressionStatus, 
        progress: Float, 
        compressedPath: String? = null, 
        compressedSize: Long = 0
    ) {
        val currentTasks = _compressionTasks.value.toMutableList()
        val index = currentTasks.indexOfFirst { it.id == id }
        if (index != -1) {
            val updatedItem = currentTasks[index].copy(
                status = status,
                progress = progress,
                compressedPath = compressedPath,
                compressedSize = compressedSize
            )
            currentTasks[index] = updatedItem
            _compressionTasks.value = currentTasks
        }
    }

    fun addTasks(items: List<MediaItem>) {
        _compressionTasks.value = _compressionTasks.value + items
    }
}
