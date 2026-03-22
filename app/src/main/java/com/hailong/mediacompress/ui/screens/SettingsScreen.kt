package com.hailong.mediacompress.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hailong.mediacompress.model.MediaItem
import com.hailong.mediacompress.model.MediaType
import com.hailong.mediacompress.ui.theme.*
import com.hailong.mediacompress.viewmodel.MediaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: MediaViewModel,
    onBackClick: () -> Unit,
    onNavigateToCompleted: () -> Unit
) {
    val selectedItems by viewModel.selectedItems.collectAsState()
    val mediaType = selectedItems.firstOrNull()?.type ?: MediaType.IMAGE

    // Image settings states
    var imageQuality by remember { mutableStateOf("中") }
    var imageFormat by remember { mutableStateOf("WebP") }
    var imageScale by remember { mutableFloatStateOf(75f) }

    // Video settings states
    var videoQuality by remember { mutableStateOf("中") }
    var videoResolution by remember { mutableStateOf("720p") }
    var removeAudio by remember { mutableStateOf(false) }

    // 计算原始总大小
    val totalOriginalSize = remember(selectedItems) {
        selectedItems.sumOf { it.size }
    }

    // 预估压缩后的大小逻辑
    val estimatedCompressedSize = remember(totalOriginalSize, imageQuality, imageFormat, imageScale, videoQuality, videoResolution, removeAudio, mediaType) {
        if (mediaType == MediaType.IMAGE) {
            val scaleFactor = (imageScale / 100f) * (imageScale / 100f) // 面积缩放
            val qualityFactor = when (imageQuality) {
                "低" -> 0.3f
                "中" -> 0.5f
                "高" -> 0.8f
                else -> 0.5f
            }
            val formatFactor = if (imageFormat == "WebP") 0.7f else 1.0f
            (totalOriginalSize * scaleFactor * qualityFactor * formatFactor).toLong()
        } else { // Video
            val qualityFactor = when (videoQuality) {
                "低" -> 0.4f
                "中" -> 0.25f
                "高" -> 0.15f
                else -> 0.25f
            }
            val resolutionFactor = when (videoResolution) {
                "480p" -> 0.25f
                "720p" -> 0.5f
                "1080p" -> 1.0f
                "原分辨率" -> 1.0f
                else -> 0.5f
            }
            (totalOriginalSize * qualityFactor * resolutionFactor).toLong()
        }
    }

    val savingsPercent = remember(totalOriginalSize, estimatedCompressedSize) {
        if (totalOriginalSize > 0) {
            ((totalOriginalSize - estimatedCompressedSize).toFloat() / totalOriginalSize * 100).toInt()
        } else 0
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
    ) {
        // Top Bar
        CenterAlignedTopAppBar(
            title = {
                Text(
                    "压缩设置",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { /* More */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = BackgroundLight
            )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            // Selected items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "已选项目",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = Color(0xFFE8F1FF),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "${selectedItems.size} 个文件",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        color = PrimaryBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(selectedItems) { item ->
                    SelectedMediaItem(item)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (mediaType) {
                MediaType.IMAGE -> ImageSettings(
                    quality = imageQuality,
                    format = imageFormat,
                    scale = imageScale,
                    onQualityChange = { imageQuality = it },
                    onFormatChange = { imageFormat = it },
                    onScaleChange = { imageScale = it }
                )
                MediaType.VIDEO -> VideoSettings(
                    quality = videoQuality,
                    resolution = videoResolution,
                    removeAudio = removeAudio,
                    onQualityChange = { videoQuality = it },
                    onResolutionChange = { videoResolution = it },
                    onRemoveAudioChange = { removeAudio = it }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Estimated Savings
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F1FF)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC5D9F9))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = PrimaryBlue
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            modifier = Modifier.padding(8.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "原大小: ${formatSize(totalOriginalSize)}", 
                            color = PrimaryBlue, 
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${formatSize(estimatedCompressedSize)} (-$savingsPercent%)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            "预计节省: ${formatSize(totalOriginalSize - estimatedCompressedSize)}",
                            color = TextGrey,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        // Bottom Action
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (mediaType == MediaType.IMAGE) {
                        val qualityInt = when (imageQuality) {
                            "低" -> 60
                            "中" -> 80
                            "高" -> 95
                            else -> 80
                        }
                        viewModel.startImageCompression(qualityInt, imageFormat, imageScale)
                    } else {
                        viewModel.startVideoCompression(videoQuality, videoResolution, removeAudio)
                    }
                    onNavigateToCompleted()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("开始压缩", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "所有压缩均在本地处理。您的图片不会离开此设备。",
                color = TextGrey,
                fontSize = 10.sp
            )
        }
    }
}

private fun formatSize(size: Long): String {
    val kb = size / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1) String.format("%.2f MB", mb) else String.format("%.2f KB", kb)
}

@Composable
fun RowScope.QualityButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .clickable { onClick() },
        color = if (isSelected) Color.White else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (isSelected) PrimaryBlue else TextGrey,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun FormatCard(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(64.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) PrimaryBlue else Color(0xFFE0E0E0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = if (isSelected) PrimaryBlue else TextGrey,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color.Transparent, CircleShape)
                        .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                )
            }
        }
    }
}


@Composable
fun ImageSettings(
    quality: String,
    format: String,
    scale: Float,
    onQualityChange: (String) -> Unit,
    onFormatChange: (String) -> Unit,
    onScaleChange: (Float) -> Unit
) {
    Column {
        // Quality
        Text(
            "压缩质量",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEDF1F7))
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                QualityButton("低", quality == "低") { onQualityChange("低") }
                QualityButton("中", quality == "中") { onQualityChange("中") }
                QualityButton("高", quality == "高") { onQualityChange("高") }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Format
        Text(
            "输出格式",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            FormatCard("WebP", format == "WebP", modifier = Modifier.weight(1f)) { onFormatChange("WebP") }
            Spacer(modifier = Modifier.width(10.dp))
            FormatCard("原格式", format == "原格式", modifier = Modifier.weight(1f)) { onFormatChange("原格式") }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Scale
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "分辨率缩放",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "${scale.toInt()}%",
                color = PrimaryBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = scale,
            onValueChange = onScaleChange,
            valueRange = 25f..100f,
            modifier = Modifier.height(28.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = PrimaryBlue,
                inactiveTrackColor = Color(0xFFE0E0E0)
            )
        )
    }
}

@Composable
fun VideoSettings(
    quality: String,
    resolution: String,
    removeAudio: Boolean,
    onQualityChange: (String) -> Unit,
    onResolutionChange: (String) -> Unit,
    onRemoveAudioChange: (Boolean) -> Unit
) {
    Column {
        // Video Quality
        Text(
            "视频质量",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEDF1F7))
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                QualityButton("低", quality == "低") { onQualityChange("低") }
                QualityButton("中", quality == "中") { onQualityChange("中") }
                QualityButton("高", quality == "高") { onQualityChange("高") }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Resolution
        Text(
            "分辨率",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            FormatCard("720p", resolution == "720p", modifier = Modifier.weight(1f)) { onResolutionChange("720p") }
            Spacer(modifier = Modifier.width(10.dp))
            FormatCard("1080p", resolution == "1080p", modifier = Modifier.weight(1f)) { onResolutionChange("1080p") }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Remove Audio
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRemoveAudioChange(!removeAudio) }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("移除音频", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text("从视频中删除音轨以减小文件大小", fontSize = 11.sp, color = TextGrey)
            }
            Switch(
                checked = removeAudio,
                onCheckedChange = onRemoveAudioChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PrimaryBlue
                )
            )
        }
    }
}

@Composable
fun SelectedMediaItem(item: MediaItem) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(20.dp)
                .clickable { /* Remove */ },
            color = Color(0x99000000),
            shape = CircleShape
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}
