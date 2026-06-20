package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.database.XPFile
import com.example.database.XPTask
import com.example.ui.theme.*
import com.example.viewmodel.XPViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ------------------------------------------------------------------------
// 1. FILE EXPLORER WITH PERMISSION ATTRIBUTES
// ------------------------------------------------------------------------
@Composable
fun ExplorerApp(viewModel: XPViewModel) {
    val currentPath by viewModel.currentPath.collectAsStateWithLifecycle()
    val allFiles by viewModel.files.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedFile = viewModel.selectedFile.value

    val subdirectories = listOf("C:\\Documents", "C:\\Windows\\System32")
    val currentFolderFiles = allFiles.filter { it.path == currentPath }

    var showPermissionsDialog by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Side Navigation Panel (Aesthetic blue pane like vintage XP!)
        Column(
            modifier = Modifier
                .width(150.dp)
                .fillMaxHeight()
                .background(Color(0xFFE4E9F5))
                .border(2.dp, Color(0xFF919B9C))
                .padding(8.dp)
        ) {
            Text(
                text = "Carpetas del Sistema",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFF001850)
            )
            Spacer(modifier = Modifier.height(8.dp))
            subdirectories.forEach { dir ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (dir == currentPath) Color(0xFFBACCE4) else Color.Transparent)
                        .clickable { viewModel.changeExplorerPath(dir) }
                        .padding(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color(0xFFFFCC33),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = dir.removePrefix("C:\\"),
                            fontSize = 11.sp,
                            fontWeight = if (dir == currentPath) FontWeight.Bold else FontWeight.Normal,
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Right Side File Display Area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            // Address bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .xpBevel(isInset = true)
                    .background(Color.White)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = " Dirección: ",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Text(
                    text = currentPath,
                    fontSize = 11.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Files Grid
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .xpBevel(isInset = true)
                    .background(Color.White)
                    .padding(4.dp)
            ) {
                if (currentFolderFiles.isEmpty()) {
                    item {
                        Text(
                            text = "Directorio vacío.",
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    items(currentFolderFiles) { file ->
                        val isSelected = selectedFile?.id == file.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSelected) Color(0xFFABC5EC) else Color.Transparent)
                                .clickable { viewModel.selectedFile.value = file }
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "File",
                                tint = if (file.canRead) Color(0xFF6699FF) else Color.Red,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = file.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (file.canRead) Color.Black else Color.Gray
                                )
                                // Displays file attributes in classic abbreviation format: [R][W][E]
                                val rStr = if (file.canRead) "R" else "-"
                                val wStr = if (file.canWrite) "W" else "-"
                                val eStr = if (file.canExecute) "E" else "-"
                                Text(
                                    text = "Tamaño: ${file.size} bytes  |  Atributos: [$rStr$wStr$eStr]",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                XpSolidButton(
                    text = "Editar (WordPad)",
                    enabled = selectedFile != null,
                    onClick = {
                        val file = selectedFile
                        if (file != null) {
                            if (!file.canRead) {
                                // Access Denied Alert
                                viewModel.openWindow("explorer_error", "Error de Acceso")
                            } else {
                                viewModel.openWindow("word_pad", "WordPad - ${file.name}")
                            }
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp)
                )

                XpSolidButton(
                    text = "Permisos de Archivo",
                    enabled = selectedFile != null,
                    onClick = { showPermissionsDialog = true }
                )
            }
        }
    }

    // Classic Windows XP File Attributes (Permissions) Properties window overlay
    if (showPermissionsDialog && selectedFile != null) {
        AlertDialog(
            onDismissRequest = { showPermissionsDialog = false },
            confirmButton = {
                XpSolidButton(text = "Finalizar", onClick = { showPermissionsDialog = false })
            },
            title = {
                Text(
                    text = "Propiedades: ${selectedFile.name}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            },
            text = {
                var cRead by remember { mutableStateOf(selectedFile.canRead) }
                var cWrite by remember { mutableStateOf(selectedFile.canWrite) }
                var cExecute by remember { mutableStateOf(selectedFile.canExecute) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(XpWindowBg)
                        .xpBevel(isInset = true)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Configurar permisos FAT32 de Windows:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Divider(modifier = Modifier.padding(bottom = 6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = cRead,
                            onCheckedChange = {
                                cRead = it
                                viewModel.updateSelectedFilePermissions(it, cWrite, cExecute)
                            }
                        )
                        Text(text = "Lectura (Read)", fontSize = 12.sp, color = Color.Black)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = cWrite,
                            onCheckedChange = {
                                cWrite = it
                                viewModel.updateSelectedFilePermissions(cRead, it, cExecute)
                            }
                        )
                        Text(text = "Escritura (Write)", fontSize = 12.sp, color = Color.Black)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = cExecute,
                            onCheckedChange = {
                                cExecute = it
                                viewModel.updateSelectedFilePermissions(cRead, cWrite, it)
                            }
                        )
                        Text(text = "Ejecución (Execute)", fontSize = 12.sp, color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Advertencia: Quitar el permiso de lectura impedirá abrir el archivo. Quitar el de escritura impedirá guardar cambios.",
                        fontSize = 10.sp,
                        color = Color.DarkGray
                    )
                }
            },
            containerColor = XpWindowBg,
            shape = RoundedCornerShape(2.dp)
        )
    }
}

// ------------------------------------------------------------------------
// 2. WORDPAD NOTE EDITOR
// ------------------------------------------------------------------------
@Composable
fun WordPadApp(viewModel: XPViewModel) {
    val selectedFile = viewModel.selectedFile.value
    var docName by remember { mutableStateOf(selectedFile?.name ?: "Sin_Titulo.txt") }
    var docContent by remember { mutableStateOf(selectedFile?.content ?: "") }

    val fileId = selectedFile?.id ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Archivo: ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            BasicTextField(
                value = docName,
                onValueChange = { docName = it },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 12.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .width(180.dp)
                    .background(Color.White)
                    .xpBevel(isInset = true)
                    .padding(4.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            XpSolidButton(
                text = "Guardar",
                onClick = {
                    if (selectedFile != null && !selectedFile.canWrite) {
                        viewModel.openWindow("explorer_write_error", "Error de Escritura")
                    } else {
                        viewModel.saveWordDocument(fileId, docName, docContent)
                        viewModel.openWindow("word_pad_saved", "Guardado")
                    }
                }
            )
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        // Document input pane
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White)
                .xpBevel(isInset = true)
                .padding(8.dp)
        ) {
            BasicTextField(
                value = docContent,
                onValueChange = { docContent = it },
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = Color.Black
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// ------------------------------------------------------------------------
// 3. RETRO PRODUCTIVITY SUITE / TASK SCHEDULER
// ------------------------------------------------------------------------
@Composable
fun TaskSchedulerApp(viewModel: XPViewModel) {
    val taskList by viewModel.tasks.collectAsStateWithLifecycle(initialValue = emptyList())

    var taskTitle by remember { mutableStateOf("") }
    var taskDesc by remember { mutableStateOf("") }
    var taskPriority by remember { mutableStateOf("Medium") }
    var taskCategory by remember { mutableStateOf("retro_office") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Form to add task
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(XpWindowBg)
                .xpBevel(isInset = false)
                .padding(8.dp)
        ) {
            Text(
                text = "Nuevo Tareas Retro Scheduler (2001)",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFF0038A6)
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Task Title
                BasicTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                    modifier = Modifier
                        .weight(1.5f)
                        .background(Color.White)
                        .xpBevel(isInset = true)
                        .padding(4.dp),
                    decorationBox = { innerTextField ->
                        if (taskTitle.isEmpty()) Text("Nombre de tarea...", color = Color.Gray, fontSize = 11.sp)
                        innerTextField()
                    }
                )

                // Task Details
                BasicTextField(
                    value = taskDesc,
                    onValueChange = { taskDesc = it },
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                    modifier = Modifier
                        .weight(2f)
                        .background(Color.White)
                        .xpBevel(isInset = true)
                        .padding(4.dp),
                    decorationBox = { innerTextField ->
                        if (taskDesc.isEmpty()) Text("Detalles del plan...", color = Color.Gray, fontSize = 11.sp)
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority picker
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Prioridad: ", fontSize = 11.sp, color = Color.Black)
                    listOf("High", "Medium", "Low").forEach { prio ->
                        val isSelected = taskPriority == prio
                        Box(
                            modifier = Modifier
                                .clickable { taskPriority = prio }
                                .background(if (isSelected) Color(0xFF90CAF9) else Color.Transparent)
                                .border(1.dp, if (isSelected) XpLunaBlue else Color.LightGray)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(text = prio, fontSize = 10.sp, color = Color.Black)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }

                XpSolidButton(
                    text = "Añadir Tarea",
                    onClick = {
                        if (taskTitle.isNotEmpty()) {
                            viewModel.createRetroTask(taskTitle, taskDesc, taskPriority, taskCategory)
                            taskTitle = ""
                            taskDesc = ""
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scheduler Database Table Grid
        Text(
            text = "Base de datos en tiempo real (Persistencia segura en SQLite):",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White)
                .xpBevel(isInset = true)
                .padding(2.dp)
        ) {
            item {
                // Grid headers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFBACCE4))
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Status", modifier = Modifier.width(50.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                    Text(text = "Título", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                    Text(text = "Prioridad", modifier = Modifier.width(62.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                    Text(text = "Acción", modifier = Modifier.width(40.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                }
            }

            items(taskList) { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            0.5.dp,
                            Color.LightGray
                        )
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Checkbox
                    Checkbox(
                        checked = task.completed,
                        onCheckedChange = { viewModel.completeRetroTask(task, it) },
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 4.dp)
                    )

                    // Title
                    Text(
                        text = task.title,
                        modifier = Modifier.weight(1.5f),
                        fontSize = 11.sp,
                        fontWeight = if (task.completed) FontWeight.Normal else FontWeight.Medium,
                        color = if (task.completed) Color.Gray else Color.Black,
                        textDecoration = if (task.completed) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Priority label
                    val pColor = when (task.priority) {
                        "High" -> Color(0xFFD32F2F)
                        "Medium" -> Color(0xFFF57C00)
                        else -> Color(0xFF388E3C)
                    }
                    Box(
                        modifier = Modifier
                            .width(55.dp)
                            .background(pColor, RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = task.priority,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Delete button
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Borrar",
                        tint = Color.Red,
                        modifier = Modifier
                            .width(40.dp)
                            .size(18.dp)
                            .clickable { viewModel.deleteRetroTask(task.id) }
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------------
// 4. LOW-POWER UTILITIES WINDOWS APP STORE
// ------------------------------------------------------------------------
@Composable
fun AppStoreApp(viewModel: XPViewModel) {
    val dynamicApps by viewModel.installedApps.collectAsStateWithLifecycle(initialValue = emptyList())
    var installAppId by remember { mutableStateOf<String?>(null) }
    var installProgress by remember { mutableStateOf(0f) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // App Store Vintage Blue banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .background(Brush.horizontalGradient(listOf(XpLunaBlueDark, XpLunaBlue)))
                .padding(6.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text(
                    text = "Tienda de Utilidades",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = "Soporte exclusivo para entornos optimizados de 4GB RAM",
                    color = Color.LightGray,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (installAppId != null) {
            // Install progress interface
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .xpBevel(isInset = false)
                    .background(XpWindowBg)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Windows Retro Installer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Copiando binarios optimizados para el disco local...",
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Custom retro progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(Color.White)
                        .border(1.dp, Color.Gray)
                        .padding(2.dp)
                ) {
                    val completedBlocks = (installProgress * 15).toInt()
                    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                        for (i in 0 until completedBlocks) {
                            Box(
                                modifier = Modifier
                                    .width(14.dp)
                                    .fillMaxHeight()
                                    .background(Brush.verticalGradient(listOf(Color(0xFF32CD32), Color(0xFF006400))))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${(installProgress * 100).toInt()}% completado",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        } else {
            // Apps list
            Text(
                text = "Paquetes de Utilidades disponibles:",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .xpBevel(isInset = true)
                    .background(Color.White)
                    .padding(4.dp)
            ) {
                val availableStoreApps = dynamicApps.filter { it.appId != "word_pad" && it.appId != "task_planner" && it.appId != "file_permissions" }
                
                items(availableStoreApps) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, Color(0xFFE0E0E0))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (app.appId) {
                                "buscaminas" -> Icons.Default.Gamepad
                                "xp_paint" -> Icons.Default.Brush
                                "calculator" -> Icons.Default.Calculate
                                else -> Icons.Default.AppShortcut
                            },
                            contentDescription = null,
                            tint = XpLunaBlue,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.appName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            val desc = when (app.appId) {
                                "buscaminas" -> "El mitico juego de logica en cuadrícula de 8x8 con 10 minas."
                                "xp_paint" -> "Cuadro de dibujo clásico de 16 colores sólidos."
                                "calculator" -> "Operaciones matemáticas de bajo consumo de ciclo de reloj."
                                else -> "Utilidad clásica retro."
                            }
                            Text(
                                text = desc,
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }

                        if (app.installed) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE0E0E0))
                                    .padding(vertical = 4.dp, horizontal = 10.dp)
                            ) {
                                Text(text = "Instalado", fontSize = 11.sp, color = Color.DarkGray)
                            }
                        } else {
                            XpSolidButton(
                                text = "Instalar",
                                onClick = {
                                    installAppId = app.appId
                                    installProgress = 0f
                                    coroutineScope.launch {
                                        for (i in 1..10) {
                                            delay(150)
                                            installProgress = i / 10f
                                        }
                                        viewModel.installAppStoreUtility(app.appId)
                                        installAppId = null
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------
// 5. WINDOWS XP TASK MANAGER & RAM CONTROL
// ------------------------------------------------------------------------
@Composable
fun TaskManagerApp(viewModel: XPViewModel) {
    val ramUsage by viewModel.ramUsage.collectAsStateWithLifecycle()
    val cpuUsage by viewModel.cpuUsage.collectAsStateWithLifecycle()
    val openWindows = viewModel.openWindows
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("Desempeño") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Tabs
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Desempeño", "Procesos", "Usuarios").forEach { tab ->
                val isSelected = activeTab == tab
                Box(
                    modifier = Modifier
                        .background(if (isSelected) XpWindowBg else Color(0xFFD2CFBF))
                        .border(1.dp, Color.Gray)
                        .clickable { activeTab = tab }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tab,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = Color.Black
                    )
                }
            }
        }

        Divider()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .xpBevel(isInset = true)
                .background(Color.White)
                .padding(8.dp)
        ) {
            when (activeTab) {
                "Desempeño" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Historial de rendimiento de la CPU:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Simulated graphs
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .background(Color.Black)
                                .border(1.dp, Color.Green),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "CPU Uso: $cpuUsage% \n(Subprocesos óptimos)",
                                color = Color.Green,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Historial de cargo de RAM física (Cota: 4096 MB):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .background(Color.Black)
                                .border(1.dp, Color.Green),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "RAM en uso: $ramUsage MB / 4096 MB\nUso del tablet: ${"%.1f".format((ramUsage / 4096f) * 100)}%",
                                color = Color.Green,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Optimización de RAM button
                        XpSolidButton(
                            text = "Optimizar RAM y Cerrar Threads inactivos",
                            onClick = { viewModel.optimizeMemory() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                "Procesos" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Tareas activas procesando en tablet:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyColumn {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.LightGray)
                                        .padding(4.dp)
                                ) {
                                    Text(text = "Nombre de Imagen", modifier = Modifier.weight(1.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text(text = "Uso de CPU", modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text(text = "Uso de RAM", modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }

                            // Dynamic active system process listed
                            item {
                                Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                                    Text(text = "explorer.exe", modifier = Modifier.weight(1.5f), fontSize = 10.sp, color = Color.Black)
                                    Text(text = "2%", modifier = Modifier.weight(1f), fontSize = 10.sp, color = Color.Black)
                                    Text(text = "124 MB", modifier = Modifier.weight(1f), fontSize = 10.sp, color = Color.Black)
                                }
                            }
                            item {
                                Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                                    Text(text = "system.dll", modifier = Modifier.weight(1.5f), fontSize = 10.sp, color = Color.Black)
                                    Text(text = "1%", modifier = Modifier.weight(1f), fontSize = 10.sp, color = Color.Black)
                                    Text(text = "512 MB", modifier = Modifier.weight(1f), fontSize = 10.sp, color = Color.Black)
                                }
                            }

                            items(openWindows) { win ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${win.contentTag}.exe",
                                        modifier = Modifier.weight(1.5f),
                                        fontSize = 10.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "4%",
                                        modifier = Modifier.weight(1f),
                                        fontSize = 10.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "64 MB",
                                        modifier = Modifier.weight(1f),
                                        fontSize = 10.sp,
                                        color = Color.Black
                                    )

                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = "End task",
                                        tint = Color.Red,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { viewModel.closeWindow(win.contentTag) }
                                    )
                                }
                            }
                        }
                    }
                }

                "Usuarios" -> {
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = XpLunaBlue,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Usuario Conectado: ${currentUser ?: "None"}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Estado: Activo",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------
// 6. BUSCAMINAS (MINESWEEPER) UTILITY
// ------------------------------------------------------------------------
@Composable
fun BuscaminasApp(viewModel: XPViewModel) {
    val status = viewModel.minesweeperStatus.value
    val minesRemaining = viewModel.minesRemaining.value
    
    // Toggle state: false = mine reveal mode, true = flag placing mode
    var flagModeEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initMinesweeper()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Mode Selector for Tablet-Optimized Single Tap Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Modo táctil: ", fontSize = 11.sp, color = Color.Black)
            XpSolidButton(
                text = "⛏️ REVELAR",
                onClick = { flagModeEnabled = false },
                modifier = Modifier
                    .padding(end = 4.dp)
                    .background(if (!flagModeEnabled) Color(0xFFABC5EC) else Color.Transparent)
            )
            XpSolidButton(
                text = "🚩 BANCO",
                onClick = { flagModeEnabled = true },
                modifier = Modifier
                    .background(if (flagModeEnabled) Color(0xFFABC5EC) else Color.Transparent)
            )
        }

        // Top yellow display board
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFC0C0C0))
                .xpBevel(isInset = true)
                .padding(vertical = 4.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mines remaining digital-styled counter
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = String.format("%03d", minesRemaining),
                    color = Color.Red,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            // Smile button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFC0C0C0))
                    .xpBevel(isInset = false)
                    .clickable { viewModel.initMinesweeper() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (status) {
                        "WON" -> "😎"
                        "LOST" -> "😵"
                        else -> "🙂"
                    },
                    fontSize = 18.sp
                )
            }

            // Fake timer
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "000",
                    color = Color.Red,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Grid Area (8x8)
        Box(
            modifier = Modifier
                .xpBevel(isInset = true)
                .background(Color(0xFF808080))
                .padding(3.dp)
        ) {
            Column {
                for (r in 0 until 8) {
                    Row {
                        for (c in 0 until 8) {
                            val idx = r * 8 + c
                            val cell = viewModel.minesweeperGrid.getOrNull(idx)
                            if (cell != null) {
                                BuscaminasCellView(
                                    cell = cell,
                                    flagModeActive = flagModeEnabled,
                                    onReveal = { viewModel.revealMinesweeperCell(idx) },
                                    onFlag = { viewModel.flagMinesweeperCell(idx) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BuscaminasCellView(
    cell: com.example.viewmodel.MinesweeperCell,
    flagModeActive: Boolean,
    onReveal: () -> Unit,
    onFlag: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(if (cell.isRevealed) Color(0xFFC0C0C0) else Color(0xFFDCD9D9))
            .border(0.5.dp, Color(0xFF808080))
            .clickable {
                if (flagModeActive) {
                    onFlag()
                } else {
                    onReveal()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (!cell.isRevealed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .xpBevel(isInset = false)
            ) {
                if (cell.isFlagged) {
                    Text(text = "🚩", fontSize = 12.sp, modifier = Modifier.align(Alignment.Center))
                }
            }
        } else {
            if (cell.isMine) {
                Text(text = "💣", fontSize = 12.sp)
            } else if (cell.adjacentMines > 0) {
                val numColor = when (cell.adjacentMines) {
                    1 -> Color.Blue
                    2 -> Color(0xFF388E3C)
                    3 -> Color.Red
                    else -> Color(0xFF7B1FA2)
                }
                Text(
                    text = cell.adjacentMines.toString(),
                    color = numColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ------------------------------------------------------------------------
// 7. XP PAINT UTILITY
// ------------------------------------------------------------------------
data class LinePath(val points: List<Offset>, val color: Color, val width: Float)

@Composable
fun PaintApp() {
    var brushColor by remember { mutableStateOf(Color.Black) }
    var strokeWidth by remember { mutableStateOf(6f) }
    val paths = remember { mutableStateListOf<LinePath>() }
    var currentPathPoints = remember { mutableStateListOf<Offset>() }

    val colors = listOf(
        Color.Black, Color.DarkGray, Color.Red, Color(0xFFFF9C00), Color.Yellow,
        Color.Green, Color.Blue, Color.Magenta, Color.White, Color.Cyan
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Canvas Painter box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White)
                .xpBevel(isInset = true)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPathPoints.add(offset)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val newPoint = change.position
                            currentPathPoints.add(newPoint)
                        },
                        onDragEnd = {
                            paths.add(LinePath(currentPathPoints.toList(), brushColor, strokeWidth))
                            currentPathPoints.clear()
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw existing paths
                paths.forEach { pathItem ->
                    if (pathItem.points.size > 1) {
                        for (i in 0 until pathItem.points.size - 1) {
                            drawLine(
                                color = pathItem.color,
                                start = pathItem.points[i],
                                end = pathItem.points[i + 1],
                                strokeWidth = pathItem.width
                            )
                        }
                    }
                }

                // Draw active path
                if (currentPathPoints.size > 1) {
                    for (i in 0 until currentPathPoints.size - 1) {
                        drawLine(
                            color = brushColor,
                            start = currentPathPoints[i],
                            end = currentPathPoints[i + 1],
                            strokeWidth = strokeWidth
                        )
                    }
                }
            }
        }

        // Color & Brush Control Toolbar (Bottom Microsoft Paint 2001 layout!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(XpWindowBg)
                .xpBevel(isInset = false)
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Colors list
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                colors.forEach { color ->
                    val isSelected = brushColor == color
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(color)
                            .border(
                                1.5.dp,
                                if (isSelected) Color.White else Color.Black,
                                RoundedCornerShape(2.dp)
                            )
                            .clickable { brushColor = color }
                    )
                }
            }

            // Brush sizes
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Tamaño: ", fontSize = 11.sp, color = Color.Black)
                listOf(4f, 8f, 15f).forEach { size ->
                    val isSelected = strokeWidth == size
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(if (isSelected) Color.LightGray else Color.Transparent)
                            .clickable { strokeWidth = size },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size((size / 2f).coerceIn(2f, 10f).dp)
                                .background(Color.Black)
                        )
                    }
                }
            }

            XpSolidButton(text = "Borrar Todo", onClick = { paths.clear() })
        }
    }
}

// ------------------------------------------------------------------------
// 8. RETRO CALCULATOR UTILITY
// ------------------------------------------------------------------------
@Composable
fun CalculatorApp() {
    var display by remember { mutableStateOf("0") }
    var currentOp by remember { mutableStateOf<String?>(null) }
    var storedVal by remember { mutableStateOf<Double?>(null) }
    var clearDisplayOnNextInput by remember { mutableStateOf(false) }

    fun processDigit(digit: String) {
        if (display == "0" || clearDisplayOnNextInput) {
            display = digit
            clearDisplayOnNextInput = false
        } else {
            display += digit
        }
    }

    fun processOp(op: String) {
        storedVal = display.toDoubleOrNull()
        currentOp = op
        clearDisplayOnNextInput = true
    }

    fun calculateResult() {
        val st = storedVal
        val curOp = currentOp
        val curVal = display.toDoubleOrNull()
        if (st != null && curOp != null && curVal != null) {
            val res = when (curOp) {
                "+" -> st + curVal
                "-" -> st - curVal
                "*" -> st * curVal
                "/" -> if (curVal != 0.0) st / curVal else "Error"
                else -> 0.0
            }
            display = if (res is Double) {
                if (res % 1 == 0.0) res.toInt().toString() else res.toString()
            } else {
                res.toString()
            }
            storedVal = null
            currentOp = null
            clearDisplayOnNextInput = true
        }
    }

    fun clearCalculator() {
        display = "0"
        storedVal = null
        currentOp = null
        clearDisplayOnNextInput = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(XpWindowBg)
            .padding(8.dp)
    ) {
        // Digital Screen (Inset gray frame)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(Color.White)
                .xpBevel(isInset = true)
                .padding(6.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = display,
                fontSize = 18.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Grid buttons
        val btns = listOf(
            listOf("7", "8", "9", "/"),
            listOf("4", "5", "6", "*"),
            listOf("1", "2", "3", "-"),
            listOf("C", "0", "=", "+")
        )

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            btns.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { char ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .background(
                                    when (char) {
                                        "C" -> Color(0xFFD32F2F)
                                        "=", "+", "-", "*", "/" -> Color(0xFFBACCE4)
                                        else -> Color(0xFFECE9D8)
                                    }
                                )
                                .xpBevel(isInset = false)
                                .clickable {
                                    when (char) {
                                        "C" -> clearCalculator()
                                        "=" -> calculateResult()
                                        "+", "-", "*", "/" -> processOp(char)
                                        else -> processDigit(char)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char,
                                fontWeight = FontWeight.Bold,
                                color = if (char == "C") Color.White else Color.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------
// INSET LOCAL VALUE WRAPPER
// ------------------------------------------------------------------------
object LocalTextStyle {
    val current: androidx.compose.ui.text.TextStyle
        @Composable
        get() = androidx.compose.material3.LocalTextStyle.current
}
