package com.example.database

import kotlinx.coroutines.flow.Flow

class XPRepository(private val xpDao: XPDao) {

    val allTasks: Flow<List<XPTask>> = xpDao.getAllTasks()
    val allApps: Flow<List<XPDynamicApp>> = xpDao.getAllApps()
    val allFiles: Flow<List<XPFile>> = xpDao.getAllFiles()

    fun getFilesByPath(path: String): Flow<List<XPFile>> {
        return xpDao.getFilesByPath(path)
    }

    suspend fun insertTask(task: XPTask) {
        xpDao.insertTask(task)
    }

    suspend fun updateTask(task: XPTask) {
        xpDao.updateTask(task)
    }

    suspend fun deleteTask(id: Int) {
        xpDao.deleteTaskById(id)
    }

    suspend fun insertFile(file: XPFile) {
        xpDao.insertFile(file)
    }

    suspend fun updateFile(file: XPFile) {
        xpDao.updateFile(file)
    }

    suspend fun deleteFile(id: Int) {
        xpDao.deleteFileById(id)
    }

    suspend fun insertApp(app: XPDynamicApp) {
        xpDao.insertApp(app)
    }

    suspend fun setAppInstalled(appId: String, installed: Boolean) {
        xpDao.setAppInstalled(appId, installed)
    }
}
