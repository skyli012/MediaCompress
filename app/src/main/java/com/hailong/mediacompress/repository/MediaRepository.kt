package com.hailong.mediacompress.repository

import android.content.Context
import android.net.Uri
import com.hailong.mediacompress.data.AppDatabase
import com.hailong.mediacompress.data.MediaItemDao
import com.hailong.mediacompress.data.MediaItemEntity
import com.hailong.mediacompress.model.CompressionStatus
import com.hailong.mediacompress.model.MediaItem
import com.hailong.mediacompress.model.MediaType
import com.hailong.mediacompress.processor.ImageProcessor
import com.hailong.mediacompress.processor.VideoProcessor
import com.hailong.mediacompress.utils.MediaUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import java.io.File

class MediaRepository(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val dao = database.mediaItemDao()
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val compressionTasks: Flow<List<MediaItem>> = dao.getAllMediaItems().map { entities ->
        entities.map { it.toMediaItem() }
    }

    private val _currentTasks = MutableStateFlow<List<MediaItem>>(emptyList())
    val currentTasks: StateFlow<List<MediaItem>> = _currentTasks

    private val outputDir: File by lazy {
        val dir = File(context.getExternalFilesDir(null), "CompressedMedia")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    suspend fun compressImages(mediaItems: List<MediaItem>, quality: Int, format: String, scale: Float) = withContext(Dispatchers.IO) {
        mediaItems.forEach { mediaItem ->
            updateTaskStatus(mediaItem.id, CompressionStatus.PROCESSING, 0f)
            
            // 处理输出格式
            val finalFormat = if (format == "原格式") {
                val ext = mediaItem.name.substringAfterLast('.', "").toUpperCase(java.util.Locale.ROOT)
                if (ext == "PNG") "PNG" else if (ext == "WEBP") "WEBP" else "JPEG"
            } else {
                format
            }

            val outputFileName = "img_${System.currentTimeMillis()}_${mediaItem.name.substringBeforeLast('.')}.${finalFormat.toLowerCase(java.util.Locale.ROOT)}"
            val outputFile = File(outputDir, outputFileName)
            
            val targetWidth = if (mediaItem.width > 0) (mediaItem.width * (scale / 100f)).toInt() else 1920
            val targetHeight = if (mediaItem.height > 0) (mediaItem.height * (scale / 100f)).toInt() else 1080

            val resultFile = ImageProcessor.compressImage(
                context,
                mediaItem.uri,
                outputFile,
                quality,
                targetWidth,
                targetHeight,
                finalFormat
            )

            if (resultFile != null && resultFile.exists() && resultFile.length() > 0) {
                val mimeType = "image/${finalFormat.toLowerCase(java.util.Locale.ROOT).let { if (it == "jpg") "jpeg" else it }}"
                MediaUtils.saveToGallery(context, resultFile, false, mimeType)
                updateTaskStatus(mediaItem.id, CompressionStatus.COMPLETED, 1f, resultFile.absolutePath, resultFile.length())
            } else {
                updateTaskStatus(mediaItem.id, CompressionStatus.FAILED, 0f)
            }
        }
    }

    suspend fun compressVideo(mediaItem: MediaItem, quality: String, resolution: String, removeAudio: Boolean) = withContext(Dispatchers.IO) {
        updateTaskStatus(mediaItem.id, CompressionStatus.PROCESSING, 0f)
        
        val outputFileName = "vid_${System.currentTimeMillis()}_${mediaItem.name}"
        val outputFile = File(outputDir, outputFileName)

        val crf = when (quality) {
            "低" -> 30
            "中" -> 26
            "高" -> 22
            else -> 26
        }

        val scale = when (resolution) {
            "720p" -> "-2:720"
            "1080p" -> "-2:1080"
            else -> null
        }
        
        VideoProcessor.compressVideo(
            context,
            mediaItem.path,
            outputFile.absolutePath,
            crf,
            scale,
            if (removeAudio) "-an" else null,
            object : VideoProcessor.VideoCompressListener {
                override fun onProgress(progress: Float) {
                    repositoryScope.launch {
                        updateTaskStatus(mediaItem.id, CompressionStatus.PROCESSING, progress)
                    }
                }

                override fun onComplete(success: Boolean, outputPath: String?) {
                    repositoryScope.launch {
                        if (success && outputPath != null) {
                            val resultFile = File(outputPath)
                            if (resultFile.exists() && resultFile.length() > 0) {
                                val ext = resultFile.extension.toLowerCase(java.util.Locale.ROOT)
                                val mimeType = "video/${if (ext == "mov") "quicktime" else "mp4"}"
                                MediaUtils.saveToGallery(context, resultFile, true, mimeType)
                                updateTaskStatus(mediaItem.id, CompressionStatus.COMPLETED, 1f, outputPath, resultFile.length())
                            } else {
                                updateTaskStatus(mediaItem.id, CompressionStatus.FAILED, 0f)
                            }
                        } else {
                            updateTaskStatus(mediaItem.id, CompressionStatus.FAILED, 0f)
                        }
                    }
                }
            }
        )
    }

    private suspend fun updateTaskStatus(
        id: Long, 
        status: CompressionStatus, 
        progress: Float, 
        compressedPath: String? = null, 
        compressedSize: Long = 0
    ) = withContext(Dispatchers.IO) {
        dao.getById(id)?.let { entity ->
            val updatedEntity = entity.copy(
                status = status,
                progress = progress,
                compressedPath = compressedPath,
                compressedSize = compressedSize
            )
            dao.update(updatedEntity)
        }
    }

    suspend fun addTasks(items: List<MediaItem>) = withContext(Dispatchers.IO) {
        items.forEach { item ->
            val id = dao.insert(item.toEntity())
            // 如果需要更新 id，这里处理。但 item.id 是根据时间生成的。
        }
    }

    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        dao.deleteAll()
    }

    private fun MediaItemEntity.toMediaItem() = MediaItem(
        id = id,
        uri = android.net.Uri.parse(uriString),
        name = name,
        path = path,
        size = size,
        type = type,
        duration = duration,
        width = width,
        height = height,
        compressedSize = compressedSize,
        compressedPath = compressedPath,
        status = status,
        progress = progress
    )

    private fun MediaItem.toEntity() = MediaItemEntity(
        id = id,
        uriString = uri.toString(),
        name = name,
        path = path,
        size = size,
        type = type,
        duration = duration,
        width = width,
        height = height,
        compressedSize = compressedSize,
        compressedPath = compressedPath,
        status = status,
        progress = progress
    )
}
