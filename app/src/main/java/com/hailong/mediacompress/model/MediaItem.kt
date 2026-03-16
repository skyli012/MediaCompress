package com.hailong.mediacompress.model

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val path: String,
    val size: Long,
    val type: MediaType,
    val duration: Long = 0, // 视频时长 (毫秒)
    val width: Int = 0,
    val height: Int = 0,
    val compressedSize: Long = 0,
    val compressedPath: String? = null,
    val status: CompressionStatus = CompressionStatus.PENDING,
    val progress: Float = 0f
)

enum class MediaType {
    IMAGE, VIDEO
}

enum class CompressionStatus {
    PENDING, PROCESSING, COMPLETED, FAILED
}
