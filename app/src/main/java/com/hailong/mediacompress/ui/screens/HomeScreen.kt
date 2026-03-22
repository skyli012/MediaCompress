package com.hailong.mediacompress.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hailong.mediacompress.model.MediaItem
import com.hailong.mediacompress.model.MediaType
import com.hailong.mediacompress.ui.theme.BackgroundLight
import com.hailong.mediacompress.ui.theme.PrimaryBlue
import com.hailong.mediacompress.ui.theme.TextDark
import com.hailong.mediacompress.ui.theme.TextGrey
import java.io.File
import java.io.FileOutputStream

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit,
    onNavigateToSettings: (List<MediaItem>) -> Unit
) {
    val context = LocalContext.current
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val items = uris.map { uri ->
                createMediaItemFromUri(context, uri, MediaType.IMAGE)
            }
            onNavigateToSettings(items)
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val item = createMediaItemFromUri(context, it, MediaType.VIDEO)
            onNavigateToSettings(listOf(item))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding() // 仅添加状态栏内边距
            .padding(horizontal = 20.dp, vertical = 4.dp) // 减小垂直内边距
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = PrimaryBlue)
            }
            Text(
                text = "媒体压缩器",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { /* Info */ }) {
                Icon(Icons.Default.Info, contentDescription = "Info", tint = TextGrey)
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // 减小间距

        Text(
            text = "优化您的媒体",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.headlineSmall, // 减小字体大小
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(4.dp)) // 减小间距

        Text(
            text = "在不损失质量的情况下缩小文件大小",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.bodySmall, // 减小字体大小
            color = TextGrey
        )

        Spacer(modifier = Modifier.height(16.dp)) // 减小间距

        // Image Card
        CompressCard(
            modifier = Modifier.weight(1f), // 使用 weight 分配剩余空间
            title = "图片压缩",
            description = "支持 JPEG, PNG, WEBP 和 HEIC 格式。采用智能优化技术。",
            icon = Icons.Default.Image,
            buttonText = "选择图片",
            onClick = { imagePickerLauncher.launch("image/*") }
        )

        Spacer(modifier = Modifier.height(16.dp)) // 减小间距

        // Video Card
        CompressCard(
            modifier = Modifier.weight(1f), // 使用 weight 分配剩余空间
            title = "视频压缩",
            description = "在保持高清分辨率的同时缩小 MP4, MOV 和 AVI 文件大小。",
            icon = Icons.Default.Videocam,
            buttonText = "选择视频",
            onClick = { videoPickerLauncher.launch("video/*") }
        )

        Spacer(modifier = Modifier.height(16.dp)) // 减小间距

        // Footer Badge
        Surface(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            shape = CircleShape,
            color = Color(0xFFE8F1FF)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), // 减小内边距
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(14.dp) // 减小图标大小
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "无需账号 • 100% 隐私安全",
                    color = PrimaryBlue,
                    fontSize = 12.sp, // 减小字体大小
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "所有处理均在您的设备上完成。",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = TextGrey,
            fontSize = 11.sp // 减小字体大小
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun CompressCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: ImageVector,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // 使图标区域自适应高度
                    .background(Color(0xFFE8F1FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp), // 减小图标大小
                    tint = PrimaryBlue
                )
            }
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) { // 减小内边距
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium, // 减小字体大小
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall, // 减小字体大小
                    color = TextGrey,
                    maxLines = 1 // 限制行数防止溢出
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp), // 减小按钮高度
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = buttonText, fontSize = 14.sp)
                }
            }
        }
    }
}

// Helper functions for Uri processing
fun createMediaItemFromUri(context: android.content.Context, uri: Uri, type: MediaType): MediaItem {
    val name = getFileName(context, uri)
    val size = getFileSize(context, uri)
    val path = if (type == MediaType.VIDEO) {
        copyUriToTempFile(context, uri, name)
    } else {
        uri.toString()
    }

    var width = 0
    var height = 0
    var duration = 0L

    try {
        if (type == MediaType.IMAGE) {
            val options = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use {
                android.graphics.BitmapFactory.decodeStream(it, null, options)
            }
            width = options.outWidth
            height = options.outHeight
        } else {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            width = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
            height = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
            duration = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
            retriever.release()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return MediaItem(
        id = System.currentTimeMillis() + uri.hashCode(),
        uri = uri,
        name = name,
        path = path,
        size = size,
        type = type,
        width = width,
        height = height,
        duration = duration
    )
}

private fun getFileName(context: android.content.Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) result = it.getString(index)
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result ?: "unknown"
}

private fun getFileSize(context: android.content.Context, uri: Uri): Long {
    var size: Long = 0
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.SIZE)
                if (index != -1) size = it.getLong(index)
            }
        }
    }
    return size
}

private fun copyUriToTempFile(context: android.content.Context, uri: Uri, fileName: String): String {
    val uniqueFileName = "temp_${System.currentTimeMillis()}_$fileName"
    val tempFile = File(context.cacheDir, uniqueFileName)
    try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return tempFile.absolutePath
}
