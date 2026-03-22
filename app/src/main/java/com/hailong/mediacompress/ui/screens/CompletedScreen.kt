package com.hailong.mediacompress.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.hailong.mediacompress.model.MediaType
import com.hailong.mediacompress.ui.theme.*
import com.hailong.mediacompress.viewmodel.MediaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

private fun formatSize(size: Long): String {
    val kb = size / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1) String.format("%.2f MB", mb) else String.format("%.2f KB", kb)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedScreen(
    modifier: Modifier = Modifier,
    viewModel: MediaViewModel,
    onBackClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val selectedItems by viewModel.selectedItems.collectAsState()
    val compressionTasks by viewModel.compressionTasks.collectAsState()
    
    val completedItems = remember(selectedItems, compressionTasks) {
        val selectedIds = selectedItems.map { it.id }.toSet()
        compressionTasks.filter { it.id in selectedIds }
    }
    
    val firstItem = completedItems.firstOrNull() ?: selectedItems.firstOrNull()
    val isProcessing = firstItem?.status == com.hailong.mediacompress.model.CompressionStatus.PROCESSING
    val isCompleted = firstItem?.status == com.hailong.mediacompress.model.CompressionStatus.COMPLETED
    val isFailed = firstItem?.status == com.hailong.mediacompress.model.CompressionStatus.FAILED

    BackHandler {
        onBackClick()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("压缩结果", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundLight)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Success/Processing/Failed Icon
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = when {
                    isCompleted -> PrimaryBlue
                    isFailed -> Color.Red
                    else -> Color(0xFFFFA500)
                }
            ) {
                Icon(
                    when {
                        isCompleted -> Icons.Default.Check
                        isFailed -> Icons.Default.Error
                        else -> Icons.Default.Loop
                    },
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                when {
                    isCompleted -> "压缩完成"
                    isFailed -> "压缩失败"
                    else -> "正在压缩..."
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                when {
                    isCompleted -> "您的媒体文件已优化完成，可以随时使用。"
                    isFailed -> "压缩过程中发生错误，请重试。"
                    else -> "正在为您压缩文件，请稍候 (${(firstItem?.progress?.times(100))?.toInt() ?: 0}%)..."
                },
                style = MaterialTheme.typography.labelSmall,
                color = TextGrey,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Result Preview Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                if (firstItem != null) {
                    Column {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color(0xFFE8F1FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (firstItem.type == MediaType.IMAGE) {
                                AsyncImage(
                                    model = firstItem.compressedPath?.let { java.io.File(it) } ?: firstItem.uri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Icon(
                                    Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(28.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE8F1FF)
                            ) {
                                Icon(
                                    if (firstItem.type == MediaType.IMAGE) Icons.Default.Image else Icons.Default.Videocam,
                                    contentDescription = null,
                                    modifier = Modifier.padding(6.dp),
                                    tint = PrimaryBlue
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    firstItem.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    if (firstItem.type == MediaType.IMAGE) "${firstItem.width} × ${firstItem.height} • ${firstItem.name.substringAfterLast('.').toUpperCase(java.util.Locale.ROOT)}" else formatDuration(firstItem.duration),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextGrey,
                                    fontSize = 10.sp
                                )
                            }
                            IconButton(
                                onClick = { /* History back icon? */ },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Size Comparison
            Row(modifier = Modifier.fillMaxWidth()) {
                ComparisonCard(
                    label = "压缩前",
                    size = formatSize(firstItem?.size ?: 0),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                ComparisonCard(
                    label = "压缩后",
                    size = formatSize(firstItem?.compressedSize ?: 0),
                    savings = firstItem?.let {
                        if (it.size > 0) "-${((it.size - it.compressedSize) * 100 / it.size)}%" else "-0%"
                    } ?: "-0%",
                    isResult = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions
            Button(
                onClick = { 
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "保存成功",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = isCompleted,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("保存到相册", fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { /* Share */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("分享", color = PrimaryBlue, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                OutlinedButton(
                    onClick = { /* More */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("更多", color = PrimaryBlue, fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ComparisonCard(label: String, size: String, savings: String? = null, isResult: Boolean = false, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, color = TextGrey, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    size,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isResult) PrimaryBlue else TextDark
                )
                savings?.let {
                    Surface(
                        color = SuccessGreen,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            it,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
