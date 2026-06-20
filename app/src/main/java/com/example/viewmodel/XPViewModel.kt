package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

// Represents an active window instance on the Windows XP desktop
data class XPWindow(
    val id: String,
    val title: String,
    val initialX: Float,
    val initialY: Float,
    val width: Int = 400, // in dp
    val height: Int = 300, // in dp
    val isMaximized: Boolean = false,
    val isMinimized: Boolean = false,
    val isFocused: Boolean = false,
    val contentTag: String // e.g. "explorer", "word_pad", "task_planner", "app_store", "task_manager", "minesweeper", "paint", "calculator"
)

enum class BootStep {
    BIOS,
    XP_LOADING,
    WELCOME_SCREEN,
    DESKTOP
}

class XPViewModel(application: Application) : AndroidViewModel(application) {

    private val database = XPDatabase.getDatabase(application)
    private val repository = XPRepository(database.xpDao())

    // UI States
    private val _bootState = MutableStateFlow(BootStep.BIOS)
    val bootState: StateFlow<BootStep> = _bootState.asStateFlow()

    private val _bootProgress = MutableStateFlow(0f)
    val bootProgress: StateFlow<Float> = _bootProgress.asStateFlow()

    private val _currentUser = MutableStateFlow<String?>("Invitado")
    val currentUser: StateFlow<String?> = _currentUser.asStateFlow()

    // RAM stats (Simulation of 4GB RAM)
    private val _ramUsage = MutableStateFlow(1240) // in MB, starting at 1.2 GB
    val ramUsage: StateFlow<Int> = _ramUsage.asStateFlow()

    private val _cpuUsage = MutableStateFlow(4) // in %
    val cpuUsage: StateFlow<Int> = _cpuUsage.asStateFlow()

    // Room Persistent lists
    val tasks = repository.allTasks
    val installedApps = repository.allApps
    val files = repository.allFiles

    // Desktop Window Manager State (Reactive memory-list)
    val openWindows = mutableStateListOf<XPWindow>()

    // Current focused file path for File Explorer
    private val _currentPath = MutableStateFlow("C:\\Documents")
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    // Selected file for edit / properties
    val selectedFile = mutableStateOf<XPFile?>(null)

    // Minesweeper board state
    val minesweeperGrid = mutableStateListOf<MinesweeperCell>()
    val minesweeperStatus = mutableStateOf("PLAYING") // PLAYING, WON, LOST
    val minesRemaining = mutableStateOf(10)

    init {
        // Prepopulate database directories, files and default apps on first startup if empty
        viewModelScope.launch {
            checkAndPrepopulateData()
            simulateSystemFluctuations()
        }
    }

    private suspend fun checkAndPrepopulateData() {
        // Pre-populate Default Apps if not exists
        val currentApps = repository.allApps.first()
        if (currentApps.isEmpty()) {
            repository.insertApp(XPDynamicApp("buscaminas", "Buscaminas (Minesweeper)", installed = false, iconName = "gamepad"))
            repository.insertApp(XPDynamicApp("xp_paint", "XP Paint Studio", installed = false, iconName = "brush"))
            repository.insertApp(XPDynamicApp("calculator", "Retro Calculator", installed = false, iconName = "calculate"))
            repository.insertApp(XPDynamicApp("word_pad", "WordPad Office", installed = true, iconName = "description"))
            repository.insertApp(XPDynamicApp("task_planner", "Productivity Planner", installed = true, iconName = "event_note"))
            repository.insertApp(XPDynamicApp("file_permissions", "File Permissions Admin", installed = true, iconName = "folder_shared"))
        }

        // Pre-populate Files if empty
        val currentFiles = repository.allFiles.first()
        if (currentFiles.isEmpty()) {
            // C:\Documents directory
            repository.insertFile(XPFile(path = "C:\\Documents", name = "Tareas_Pendientes.txt", content = "Suite de Productividad Retro - Tareas urgentes:\n- Revisar el presupuesto anual FAT32 en Excel\n- Organizar las utilidades de bajo consumo en la tienda\n- Verificar la optimizacion de RAM para soporte de 4GB.", canRead = true, canWrite = true, canExecute = true))
            repository.insertFile(XPFile(path = "C:\\Documents", name = "Receta_Secreta.doc", content = "Ingredientes para un buen Windows XP:\n1. Bliss wallpaper\n2. Boton Inicio verde brillante\n3. Barra de tareas azul brillante Azul Luna\n4. Sonido de bienvenida clasico", canRead = true, canWrite = false, canExecute = true)) // Read-only
            repository.insertFile(XPFile(path = "C:\\Documents", name = "Sin_Lectura.txt", content = "ESTO ES PRIVADO! No deberias poder leer esto si desactivas el permiso de lectura.", canRead = false, canWrite = true, canExecute = true)) // Unreadable
            
            // C:\Windows\System32 directory
            repository.insertFile(XPFile(path = "C:\\Windows\\System32", name = "kernel32.dll", content = "[KERNEL32] Entrypoints loaded successfully. Memory limits set to 4096MB RAM safely.", canRead = true, canWrite = false, canExecute = true))
            repository.insertFile(XPFile(path = "C:\\Windows\\System32", name = "shutdown.exe", content = "Cerrando sistema retro...", canRead = true, canWrite = true, canExecute = false)) // Unexecutable (can't double click program)
            
            // Default welcome tasks
            repository.insertTask(XPTask(title = "Reparar impresora retro", description = "El puerto LPT1 no esta respondiendo en la maquina virtual.", priority = "Medium", completed = false, category = "Oficina"))
            repository.insertTask(XPTask(title = "Optimizar RAM de 4GB", description = "Liberar procesos innecesarios para garantizar la velocidad de arranque ultrarrapida en tablets.", priority = "High", completed = true, category = "Sistema"))
            repository.insertTask(XPTask(title = "Jugar al Buscaminas", description = "Romper el record de los clasicos 10 segundos en nivel principiante.", priority = "Low", completed = false, category = "Personal"))
        }
    }

    private suspend fun simulateSystemFluctuations() {
        // Continuous loop to simulate CPU and RAM usage to look completely alive!
        while (true) {
            delay(1500)
            if (_bootState.value == BootStep.DESKTOP) {
                val offset = Random.nextInt(-20, 21)
                val currentRam = _ramUsage.value
                val newRam = (currentRam + offset).coerceIn(1100, 1550)
                _ramUsage.value = newRam

                val newCpu = Random.nextInt(1, 15)
                _cpuUsage.value = if (openWindows.isNotEmpty()) newCpu + (openWindows.size * 3) else newCpu
            }
        }
    }

    // --- Boot Operations ---
    fun startBootSequence() {
        viewModelScope.launch {
            _bootState.value = BootStep.BIOS
            delay(1200) // Simulated quick bios screen
            _bootState.value = BootStep.XP_LOADING
            
            // fast loading progress
            for (i in 1..100 step 10) {
                _bootProgress.value = i / 100f
                delay(120) // About 1.2s total boot! Extremely fast!
            }
            _bootState.value = BootStep.WELCOME_SCREEN
        }
    }

    fun loginUser(username: String) {
        _currentUser.value = username
        _bootState.value = BootStep.DESKTOP
    }

    fun triggerSignOut() {
        viewModelScope.launch {
            closeAllWindows()
            _currentUser.value = null
            _bootState.value = BootStep.WELCOME_SCREEN
        }
    }

    fun triggerShutdown() {
        viewModelScope.launch {
            closeAllWindows()
            _bootState.value = BootStep.BIOS
            delay(1000)
            startBootSequence()
        }
    }

    // --- Window Manager Operations ---
    fun openWindow(contentTag: String, title: String) {
        // Check if already open
        val existingIndex = openWindows.indexOfFirst { it.contentTag == contentTag }
        if (existingIndex != -1) {
            // Bring to focus
            val win = openWindows[existingIndex]
            openWindows.removeAt(existingIndex)
            openWindows.add(win.copy(isMinimized = false, isFocused = true))
            unfocusOthers(contentTag)
            return
        }

        // Generate coordinate offsets so windows don't overlap perfectly
        val count = openWindows.size
        val xOffset = 30f + (count * 25) % 150
        val yOffset = 30f + (count * 25) % 150

        // Determine size
        val (width, height) = when (contentTag) {
            "explorer" -> Pair(520, 380)
            "word_pad" -> Pair(550, 420)
            "task_planner" -> Pair(620, 440)
            "app_store" -> Pair(480, 400)
            "task_manager" -> Pair(410, 480)
            "minesweeper" -> Pair(330, 410)
            "paint" -> Pair(580, 460)
            "calculator" -> Pair(280, 410)
            else -> Pair(400, 300)
        }

        val newWindow = XPWindow(
            id = System.currentTimeMillis().toString(),
            title = title,
            initialX = xOffset,
            initialY = yOffset,
            width = width,
            height = height,
            isFocused = true,
            contentTag = contentTag
        )

        openWindows.add(newWindow)
        unfocusOthers(newWindow.contentTag)
    }

    fun closeWindow(contentTag: String) {
        openWindows.removeAll { it.contentTag == contentTag }
    }

    fun minimizeWindow(contentTag: String) {
        val idx = openWindows.indexOfFirst { it.contentTag == contentTag }
        if (idx != -1) {
            val win = openWindows[idx]
            openWindows[idx] = win.copy(isMinimized = true, isFocused = false)
        }
    }

    fun maximizeWindow(contentTag: String) {
        val idx = openWindows.indexOfFirst { it.contentTag == contentTag }
        if (idx != -1) {
            val win = openWindows[idx]
            openWindows[idx] = win.copy(isMaximized = !win.isMaximized)
        }
    }

    fun focusWindow(contentTag: String) {
        val idx = openWindows.indexOfFirst { it.contentTag == contentTag }
        if (idx != -1) {
            val win = openWindows[idx]
            openWindows.removeAt(idx)
            openWindows.add(win.copy(isMinimized = false, isFocused = true))
            unfocusOthers(contentTag)
        }
    }

    private fun unfocusOthers(activeTag: String) {
        for (i in 0 until openWindows.size) {
            val win = openWindows[i]
            if (win.contentTag != activeTag && win.isFocused) {
                openWindows[i] = win.copy(isFocused = false)
            }
        }
    }

    fun closeAllWindows() {
        openWindows.clear()
    }

    // --- RAM Optimizer ---
    fun optimizeMemory() {
        viewModelScope.launch {
            _cpuUsage.value = 98 // Simulated heavy compression!
            delay(800)
            _ramUsage.value = Random.nextInt(780, 890) // Liberates down to 800MB!
            _cpuUsage.value = 2
        }
    }

    // --- File System Operations ---
    fun changeExplorerPath(newPath: String) {
        _currentPath.value = newPath
        selectedFile.value = null
    }

    fun updateSelectedFilePermissions(canRead: Boolean, canWrite: Boolean, canExecute: Boolean) {
        val file = selectedFile.value ?: return
        val updated = file.copy(canRead = canRead, canWrite = canWrite, canExecute = canExecute)
        selectedFile.value = updated
        viewModelScope.launch {
            repository.updateFile(updated)
        }
    }

    fun saveWordDocument(fileId: Int, name: String, content: String) {
        viewModelScope.launch {
            val target = SelectedFile()
            if (fileId != 0) {
                // Edit existing
                val existing = repository.allFiles.first().firstOrNull { it.id == fileId }
                if (existing != null) {
                    repository.updateFile(existing.copy(name = name, content = content, size = content.length.toLong()))
                }
            } else {
                // New file
                repository.insertFile(XPFile(path = _currentPath.value, name = name, content = content, canRead = true, canWrite = true, canExecute = true))
            }
        }
    }

    private fun SelectedFile(): XPFile? {
        return selectedFile.value
    }

    // --- Task Productivity Suit ---
    fun createRetroTask(title: String, description: String, priority: String, category: String) {
        viewModelScope.launch {
            repository.insertTask(XPTask(title = title, description = description, priority = priority, category = category))
        }
    }

    fun completeRetroTask(task: XPTask, completed: Boolean) {
        viewModelScope.launch {
            repository.updateTask(task.copy(completed = completed))
        }
    }

    fun deleteRetroTask(taskId: Int) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    // --- Low-Power App Store installation ---
    fun installAppStoreUtility(appId: String) {
        viewModelScope.launch {
            repository.setAppInstalled(appId, true)
            // Simulated installer dialogue
            delay(100)
        }
    }

    // --- Buscaminas Controller Logic ---
    fun initMinesweeper() {
        minesweeperStatus.value = "PLAYING"
        minesRemaining.value = 10
        minesweeperGrid.clear()
        
        // 8x8 Grid with 10 random mines
        val minePositions = mutableSetOf<Int>()
        while (minePositions.size < 10) {
            minePositions.add(Random.nextInt(0, 64))
        }

        for (i in 0 until 64) {
            val r = i / 8
            val c = i % 8
            minesweeperGrid.add(
                MinesweeperCell(
                    index = i,
                    row = r,
                    col = c,
                    isMine = minePositions.contains(i),
                    isRevealed = false,
                    isFlagged = false,
                    adjacentMines = 0
                )
            )
        }

        // Calculate adjacent counts
        for (i in 0 until 64) {
            if (minesweeperGrid[i].isMine) continue
            var count = 0
            val r = minesweeperGrid[i].row
            val c = minesweeperGrid[i].col
            
            for (dr in -1..1) {
                for (dc in -1..1) {
                    val nr = r + dr
                    val nc = c + dc
                    if (nr in 0..7 && nc in 0..7) {
                        val neighborIdx = nr * 8 + nc
                        if (minesweeperGrid[neighborIdx].isMine) {
                            count++
                        }
                    }
                }
            }
            minesweeperGrid[i] = minesweeperGrid[i].copy(adjacentMines = count)
        }
    }

    fun revealMinesweeperCell(idx: Int) {
        if (minesweeperStatus.value != "PLAYING") return
        val cell = minesweeperGrid[idx]
        if (cell.isRevealed || cell.isFlagged) return

        if (cell.isMine) {
            // Blow up! Show all mines
            minesweeperStatus.value = "LOST"
            for (i in 0 until 64) {
                if (minesweeperGrid[i].isMine) {
                    minesweeperGrid[i] = minesweeperGrid[i].copy(isRevealed = true)
                }
            }
            return
        }

        // Normal cell reveal
        revealRecursively(idx)

        // Check Win Condition
        val unrevealedSafeCells = minesweeperGrid.any { !it.isMine && !it.isRevealed }
        if (!unrevealedSafeCells) {
            minesweeperStatus.value = "WON"
            minesRemaining.value = 0
        }
    }

    private fun revealRecursively(idx: Int) {
        val cell = minesweeperGrid[idx]
        if (cell.isRevealed || cell.isFlagged) return
        minesweeperGrid[idx] = cell.copy(isRevealed = true)

        if (cell.adjacentMines == 0) {
            // Reveal automatic neighbors
            val r = cell.row
            val c = cell.col
            for (dr in -1..1) {
                for (dc in -1..1) {
                    val nr = r + dr
                    val nc = c + dc
                    if (nr in 0..7 && nc in 0..7) {
                        val nIdx = nr * 8 + nc
                        if (!minesweeperGrid[nIdx].isRevealed) {
                            revealRecursively(nIdx)
                        }
                    }
                }
            }
        }
    }

    fun flagMinesweeperCell(idx: Int) {
        if (minesweeperStatus.value != "PLAYING") return
        val cell = minesweeperGrid[idx]
        if (cell.isRevealed) return

        val newFlagState = !cell.isFlagged
        minesweeperGrid[idx] = cell.copy(isFlagged = newFlagState)
        minesRemaining.value += if (newFlagState) -1 else 1
    }
}

data class MinesweeperCell(
    val index: Int,
    val row: Int,
    val col: Int,
    val isMine: Boolean,
    val isRevealed: Boolean,
    val isFlagged: Boolean,
    val adjacentMines: Int
)
