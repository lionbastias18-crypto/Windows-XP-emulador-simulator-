package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.theme.*
import com.example.viewmodel.BootStep
import com.example.viewmodel.XPViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun XPRetroDesktop(viewModel: XPViewModel) {
    val bootState by viewModel.bootState.collectAsStateWithLifecycle()
    val bootProgress by viewModel.bootProgress.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        when (bootState) {
            BootStep.BIOS -> BIOSScreen(onSkip = { viewModel.loginUser("Administrator") })
            BootStep.XP_LOADING -> XPLoadingScreen(progress = bootProgress)
            BootStep.WELCOME_SCREEN -> WelcomeScreen(viewModel = viewModel)
            BootStep.DESKTOP -> DesktopWorkspaceScreen(viewModel = viewModel)
        }
    }
}

// ------------------------------------------------------------------------
// A. INTEL BIOS QUICK BOOT BOARDS SCREEN
// ------------------------------------------------------------------------
@Composable
fun BIOSScreen(onSkip: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onSkip() }
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "AMIBIOS (C) 2001 American Megatrends, Inc.\nASUS K7M Motherboard Retro Edition\nCPU: AMD Athlon(tm) Processor - 1000MHz\nTesting System RAM: 4194304KB OK (Dual Channel)",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                // Red award emblem
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .background(Color.Red)
                        .border(1.dp, Color.Yellow)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "BIOS", color = Color.Yellow, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Primary Master: Maxtor 4D040H2 40GB FAT32\nPrimary Slave: LG CD-ROM CRD-8524B\nKeyboard: USB Touch Controller Node Detected.\nGestures & Multi-touch engine initialized.\n\nPress any key or Tap Screen to Quick Boot XP...",
                color = Color.LightGray,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(text = "American Megatrends\n06/19/2001-VT82C686-ASUS_K7M", color = Color.DarkGray, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                Text(text = "Arranque veloz activado", color = Color.Green, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ------------------------------------------------------------------------
// B. CLASSIC WINDOWS XP LOADING/PROGRESS JUMP BAR
// ------------------------------------------------------------------------
@Composable
fun XPLoadingScreen(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Windows XP Colorful text Logo
            Text(
                text = "Microsoft",
                color = Color.LightGray,
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Windows XP",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.height(30.dp))

            // Progress bar box frame
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(18.dp)
                    .border(2.dp, Color(0xFF9E9E9E), RoundedCornerShape(4.dp))
                    .padding(2.dp)
            ) {
                // Moving blue blocks
                val infiniteTransition = rememberInfiniteTransition()
                val offsetFraction by infiniteTransition.animateFloat(
                    initialValue = -0.2f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val barWidth = maxWidth
                    val blockWidth = 24.dp
                    val maxOffset = barWidth - blockWidth
                    val currentOffset = maxOffset * offsetFraction.coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier
                            .offset(x = currentOffset)
                            .width(blockWidth)
                            .fillMaxHeight(),
                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        for (i in 0..2) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(Color(0xFF3860F4), RoundedCornerShape(1.dp))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "Copyright (C) Microsoft Corporation",
                color = Color.Gray,
                fontSize = 11.sp,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(top = 100.dp)
            )
        }
    }
}

// ------------------------------------------------------------------------
// C. PROFESSIONAL WELCOME USER LOGIN PORTAL
// ------------------------------------------------------------------------
@Composable
fun WelcomeScreen(viewModel: XPViewModel) {
    val players = listOf(
        Pair("Administrador", Icons.Default.Lock),
        Pair("Invitado", Icons.Default.Person)
    )

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Column (Blue Header Panel)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .background(Brush.verticalGradient(listOf(Color(0xFF0038A6), Color(0xFF0F5CE3))))
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "Windows XP",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.End
            )
            Text(
                text = "Professional",
                color = Color(0xFFBACCE4),
                fontSize = 16.sp,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.End
            )
        }

        // Center split vertical white/yellow line
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xFFFFCC33), Color.Transparent)))
        )

        // Right Column (Accounts List)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1.3f)
                .background(Brush.verticalGradient(listOf(Color(0xFF5A8EE4), Color(0xFF1E5EC2))))
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Haga clic en su nombre de usuario para comenzar",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            players.forEach { (user, icon) ->
                Row(
                    modifier = Modifier
                        .width(260.dp)
                        .padding(vertical = 8.dp)
                        .border(1.dp, Color.Transparent)
                        .clickable { viewModel.loginUser(user) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile picture avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White)
                            .border(2.dp, Color(0xFFBACCE4), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = user,
                            tint = Color(0xFF1E5EC2),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = user,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Haga clic para iniciar sesión",
                            color = Color(0xFFDCDCDC),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------
// D. MAIN WORKSPACE / THE XP DESKTOP
// ------------------------------------------------------------------------
@Composable
fun DesktopWorkspaceScreen(viewModel: XPViewModel) {
    val ramLoad by viewModel.ramUsage.collectAsStateWithLifecycle()
    val cpuLoad by viewModel.cpuUsage.collectAsStateWithLifecycle()
    val openWindows = viewModel.openWindows
    val currentPath by viewModel.currentPath.collectAsStateWithLifecycle()
    val dynamicApps by viewModel.installedApps.collectAsStateWithLifecycle(initialValue = emptyList())

    val clockTime = rememberClockTime()

    var showStartMenu by remember { mutableStateOf(false) }

    // Multi-touch gestures variables
    var scale by remember { mutableStateOf(1f) }
    var offsetState by remember { mutableStateOf(Offset.Zero) }
    val gestureState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.8f, 1.5f)
        offsetState += offsetChange
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .transformable(state = gestureState) // Gestures multitoque fluidos
    ) {
        // Bliss Background Image
        Image(
            painter = painterResource(id = R.drawable.img_xp_bliss_1781913880186),
            contentDescription = "Desktop Bliss Background",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetState.x,
                    translationY = offsetState.y
                ),
            contentScale = ContentScale.Crop
        )

        // Desktop Icons Layout Grid
        Column(
            modifier = Modifier
                .padding(16.dp)
                .width(220.dp)
                .fillMaxHeight(0.85f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DesktopShortcut(
                title = "Mi PC (Explorer)",
                icon = Icons.Default.Computer,
                onClick = { viewModel.openWindow("explorer", "Explorador de Archivos - NTFS C:\\") }
            )

            DesktopShortcut(
                title = "Mis Tareas",
                icon = Icons.Default.EventNote,
                onClick = { viewModel.openWindow("task_planner", "Administrador de Tareas Clásico") }
            )

            DesktopShortcut(
                title = "App Store Retro",
                icon = Icons.Default.Storefront,
                onClick = { viewModel.openWindow("app_store", "Retro App Store - Bajo Consumo") }
            )

            DesktopShortcut(
                title = "Administrador RAM",
                icon = Icons.Default.Memory,
                onClick = { viewModel.openWindow("task_manager", "Administrador de Tareas de Windows") }
            )

            // Dynamic installed apps listed dynamically as shortcuts
            dynamicApps.filter { it.installed }.forEach { app ->
                val alreadyManagedIcon = app.appId == "word_pad" || app.appId == "task_planner" || app.appId == "file_permissions"
                if (!alreadyManagedIcon) {
                    DesktopShortcut(
                        title = app.appName.split(" (")[0],
                        icon = when (app.appId) {
                            "buscaminas" -> Icons.Default.Gamepad
                            "xp_paint" -> Icons.Default.Brush
                            "calculator" -> Icons.Default.Calculate
                            else -> Icons.Default.AppShortcut
                        },
                        onClick = { viewModel.openWindow(app.appId, app.appName) }
                    )
                }
            }
        }

        // Draggable floating windows workspace rendering layer
        openWindows.forEach { win ->
            if (!win.isMinimized) {
                key(win.id) {
                    XpWindowFrame(
                        title = win.title,
                        isFocused = win.isFocused,
                        isMaximized = win.isMaximized,
                        onClose = { viewModel.closeWindow(win.contentTag) },
                        onMinimize = { viewModel.minimizeWindow(win.contentTag) },
                        onMaximize = { viewModel.maximizeWindow(win.contentTag) },
                        onFocus = { viewModel.focusWindow(win.contentTag) },
                        initialX = win.initialX,
                        initialY = win.initialY,
                        widthDp = win.width,
                        heightDp = win.height
                    ) {
                        // Dynamically render application content inside the generic window box
                        when (win.contentTag) {
                            "explorer" -> ExplorerApp(viewModel = viewModel)
                            "word_pad" -> WordPadApp(viewModel = viewModel)
                            "task_planner" -> TaskSchedulerApp(viewModel = viewModel)
                            "app_store" -> AppStoreApp(viewModel = viewModel)
                            "task_manager" -> TaskManagerApp(viewModel = viewModel)
                            "minesweeper" -> BuscaminasApp(viewModel = viewModel)
                            "paint" -> PaintApp()
                            "calculator" -> CalculatorApp()
                            "explorer_error" -> {
                                AccessDeniedNotice(title = "Acceso Denegado", text = "Permiso de LECTURA (Read) desactivado. No es posible abrir y procesar la información del archivo.")
                            }
                            "explorer_write_error" -> {
                                AccessDeniedNotice(title = "Escritura denegada", text = "Permiso de ESCRITURA (Write) desactivado. El archivo es de solo lectura.")
                            }
                            "word_pad_saved" -> {
                                ActionSuccessNotice(text = "Cambios guardados con éxito en la base de datos local pre-parada.")
                            }
                        }
                    }
                }
            }
        }

        // --- Start Menu Drawer popup ---
        if (showStartMenu) {
            StartMenuPopup(
                viewModel = viewModel,
                onClose = { showStartMenu = false },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 40.dp, start = 2.dp)
            )
        }

        // --- Blue Taskbar across bottom ---
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(40.dp)
                .background(Brush.verticalGradient(listOf(XpLunaBlueLight, XpLunaBlue, XpLunaBlueDark)))
                .border(1.dp, Color(0xFF5A8EE4)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // green Inicio button
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(XpStartGreen, XpStartGreenDark)
                        ),
                        RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
                    )
                    .clickable { showStartMenu = !showStartMenu },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Launch,
                        contentDescription = "Inicio Logo",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "inicio",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp
                    )
                }
            }

            // Opened taskbar window tab buttons row
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                openWindows.forEach { win ->
                    val isFocused = win.isFocused && !win.isMinimized
                    val tabColor = if (isFocused) Color(0xFF1E52C2) else Color(0xFF3C7BE0)
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .fillMaxHeight(0.85f)
                            .clip(RoundedCornerShape(3.dp))
                            .background(tabColor)
                            .border(1.dp, Color.White.copy(0.3f), RoundedCornerShape(3.dp))
                            .clickable {
                                if (isFocused) {
                                    viewModel.minimizeWindow(win.contentTag)
                                } else {
                                    viewModel.focusWindow(win.contentTag)
                                }
                            }
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = win.title.split(" - ").last(),
                            color = Color.White,
                            fontSize = 10.sp,
                            maxLines = 1,
                            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // System tray area with Clock and simulated RAM usage info
            Row(
                modifier = Modifier
                    .width(180.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF0F8FE3))
                    .border(1.dp, Color.White.copy(0.5f)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Simulated RAM usage load indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "RAM limit",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${"%.1f".format(ramLoad / 1024f)}G / 4G",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Clock time
                Text(
                    text = clockTime,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
    }
}

@Composable
fun DesktopShortcut(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(Color.White.copy(0.18f), CircleShape)
                .border(1.dp, Color.White.copy(0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(color = Color.Black, blurRadius = 4f)
            ),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ------------------------------------------------------------------------
// E. WINDOWS XP START MENU POPUP COMPONENT STYLE
// ------------------------------------------------------------------------
@Composable
fun StartMenuPopup(
    viewModel: XPViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .width(320.dp)
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .border(2.dp, XpLunaBlue, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .background(Color.White)
    ) {
        Column {
            // Top Header Panel (User ID Profile)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(XpLunaBlueDark, XpLunaBlue)))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "User Icon",
                        tint = XpLunaBlue,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = currentUser ?: "Administrator",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            // Left App List & Right Special Folder list split panel
            Row(modifier = Modifier.fillMaxWidth()) {
                // Left Apps shortcuts
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Programas Frecuentes",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )

                    StartMenuItem(
                        title = "WordPad Office",
                        icon = Icons.Default.HistoryEdu,
                        onClick = {
                            onClose()
                            viewModel.openWindow("word_pad", "WordPad Office file")
                        }
                    )

                    StartMenuItem(
                        title = "Task Planner Suite",
                        icon = Icons.Default.EventNote,
                        onClick = {
                            onClose()
                            viewModel.openWindow("task_planner", "Administrador de Tareas Clásico")
                        }
                    )

                    StartMenuItem(
                        title = "Store de Utilidades",
                        icon = Icons.Default.Storefront,
                        onClick = {
                            onClose()
                            viewModel.openWindow("app_store", "Retro App Store - Bajo Consumo")
                        }
                    )

                    StartMenuItem(
                        title = "Minesweeper",
                        icon = Icons.Default.Gamepad,
                        onClick = {
                            onClose()
                            viewModel.openWindow("minesweeper", "Buscaminas (Minesweeper)")
                        }
                    )
                }

                // Right directories layout list
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFBACCE4))
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StartMenuItem(
                        title = "Mis Documentos",
                        icon = Icons.Default.FolderOpen,
                        onClick = {
                            onClose()
                            viewModel.openWindow("explorer", "Explorador de Archivos - NTFS C:\\")
                        }
                    )
                    StartMenuItem(
                        title = "Soporte Técnico RAM",
                        icon = Icons.Default.Build,
                        onClick = {
                            onClose()
                            viewModel.openWindow("task_manager", "Administrador de Tareas de Windows")
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    Divider(color = Color.LightGray)

                    StartMenuItem(
                        title = "Liberar RAM",
                        icon = Icons.Default.Bolt,
                        onClick = {
                            viewModel.optimizeMemory()
                            onClose()
                        }
                    )
                }
            }

            // Footer bar (Cerrar sesión & Apagar equipo buttons!)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F8FE3))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable {
                        onClose()
                        viewModel.triggerSignOut()
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Log Off", tint = Color.White, modifier = Modifier.size(16.dp))
                    Text(text = " Cerrar sesión", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.clickable {
                        onClose()
                        viewModel.triggerShutdown()
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Power Off", tint = Color(0xFFF12E11), modifier = Modifier.size(16.dp))
                    Text(text = " Reiniciar BIOS/XP", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StartMenuItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = XpLunaBlue, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = title, fontSize = 11.sp, color = Color.Black)
    }
}

// ------------------------------------------------------------------------
// ERRORS OR NOTICES WINDOW INSERTS
// ------------------------------------------------------------------------
@Composable
fun AccessDeniedNotice(title: String, text: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Cancel,
            contentDescription = "Alert Error",
            tint = Color.Red,
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            color = Color.Red,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ActionSuccessNotice(text: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success check",
            tint = Color(0xFF42B300),
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Acción Completa",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF42B300),
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

// Helper Clock state
@Composable
fun rememberClockTime(): String {
    var formattedTime by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val date = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            formattedTime = date
            delay(1000)
        }
    }
    return formattedTime
}
