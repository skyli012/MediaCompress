package com.hailong.mediacompress

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import com.hailong.mediacompress.ui.screens.*
import com.hailong.mediacompress.ui.theme.MediaCompressTheme
import com.hailong.mediacompress.ui.theme.PrimaryBlue
import com.hailong.mediacompress.ui.theme.TextGrey
import com.hailong.mediacompress.viewmodel.MediaViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MediaViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "部分权限被拒绝，某些功能可能无法使用。", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 配置 Coil 支持视频帧解码
        val imageLoader = ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
        coil.Coil.setImageLoader(imageLoader)

        setContent {
            MediaCompressTheme {
                MainApp(viewModel)
            }
        }

        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        val toRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (toRequest.isNotEmpty()) {
            permissionLauncher.launch(toRequest.toTypedArray())
        }
    }
}

@Composable
fun MainApp(viewModel: MediaViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 设置开关状态
    var keepOriginal by remember { mutableStateOf(true) }

    // 将设置传递给 ViewModel
    LaunchedEffect(keepOriginal) {
        viewModel.setKeepOriginal(keepOriginal)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = Color.White
            ) {
                Spacer(Modifier.height(48.dp))
                Text(
                    "设置",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider()
                
                // 侧边栏中的具体设置开关
                SettingsSwitchItem(
                    title = "保留原文件",
                    description = "压缩后不删除原始媒体文件",
                    checked = keepOriginal,
                    onCheckedChange = { keepOriginal = it }
                )

                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    "版本 1.0.0",
                    modifier = Modifier.padding(16.dp),
                    color = TextGrey,
                    fontSize = 12.sp
                )
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                // 仅在首页和历史页面显示底部导航
                val showBottomBar = currentDestination?.route in listOf("home", "history")
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = Color.White,
                        contentColor = PrimaryBlue,
                        tonalElevation = 0.dp // 移除色调提升，保持纯白
                    ) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = "首页") },
                            label = { Text("首页") },
                            selected = currentDestination?.hierarchy?.any { it.route == "home" } == true,
                            onClick = {
                                navController.navigate("home") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.History, contentDescription = "历史") },
                            label = { Text("历史") },
                            selected = currentDestination?.hierarchy?.any { it.route == "history" } == true,
                            onClick = {
                                navController.navigate("history") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController, 
                startDestination = "home",
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                }
            ) {
                composable("home") {
                    HomeScreen(
                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onNavigateToSettings = { items ->
                            viewModel.addTasks(items)
                            navController.navigate("settings")
                        }
                    )
                }
                composable("history") {
                    HistoryScreen(
                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                        viewModel = viewModel
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() },
                        onNavigateToCompleted = { 
                            navController.navigate("completed") {
                                popUpTo("settings") { inclusive = true }
                            }
                        }
                    )
                }
                composable("completed") {
                    CompletedScreen(
                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack("home", inclusive = false) }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = description, color = TextGrey, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryBlue
            )
        )
    }
}
