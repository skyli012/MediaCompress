package com.hailong.mediacompress.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hailong.mediacompress.model.MediaItem
import com.hailong.mediacompress.model.MediaType
import com.hailong.mediacompress.ui.theme.*
import com.hailong.mediacompress.viewmodel.MediaViewModel

import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: MediaViewModel
) {
    val tasks by viewModel.compressionTasks.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Tab 选中状态
    var selectedTabIndex by remember { mutableStateOf(0) }

    // 根据选中的 Tab 过滤数据
    val filteredTasks = remember(selectedTabIndex, tasks) {
        when (selectedTabIndex) {
            0 -> tasks // 全部
            1 -> tasks.filter { it.type == MediaType.IMAGE } // 图片
            2 -> tasks.filter { it.type == MediaType.VIDEO } // 视频
            else -> tasks
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除所有历史记录吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showDeleteDialog = false
                    }
                ) {
                    Text("确认", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
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
                    "历史记录",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                if (filteredTasks.isNotEmpty()) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete all")
                    }
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = BackgroundLight
            )
        )

        // TabRow - 支持点击切换
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = BackgroundLight,
            contentColor = PrimaryBlue,
            divider = {}
        ) {
            // 全部 Tab
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Text(
                        "全部",
                        color = if (selectedTabIndex == 0) PrimaryBlue else TextGrey,
                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            // 图片 Tab
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Text(
                        "图片",
                        color = if (selectedTabIndex == 1) PrimaryBlue else TextGrey,
                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            // 视频 Tab
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                text = {
                    Text(
                        "视频",
                        color = if (selectedTabIndex == 2) PrimaryBlue else TextGrey,
                        fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }

        // 根据选中的 Tab 显示对应的内容
        if (filteredTasks.isEmpty()) {
            // 空状态展示
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = when (selectedTabIndex) {
                            0 -> "暂无历史记录"
                            1 -> "暂无图片记录"
                            2 -> "暂无视频记录"
                            else -> "暂无记录"
                        },
                        color = TextGrey,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "压缩后的文件将显示在这里",
                        color = TextGrey.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTasks) { task ->
                    HistoryItem(task)
                }
            }
        }
    }
}

@Composable
fun HistoryItem(item: MediaItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = if (item.compressedPath != null) item.compressedPath else item.uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Format Tag
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp),
                    color = Color(0x99000000),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (item.type == MediaType.VIDEO) "MP4" else "JPG",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatSize(item.size),
                        color = TextGrey,
                        fontSize = 13.sp
                    )
                    Icon(
                        Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier
                            .size(14.dp)
                            .padding(horizontal = 4.dp)
                    )
                    Text(
                        text = if (item.compressedSize > 0) formatSize(item.compressedSize) else "---",
                        color = PrimaryBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTime(item.id),
                    color = TextGrey,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = { /* More */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = TextGrey)
            }
        }
    }
}

private fun formatSize(size: Long): String {
    val kb = size / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1) String.format("%.2f MB", mb) else String.format("%.2f KB", kb)
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}