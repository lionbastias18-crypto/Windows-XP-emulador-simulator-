package com.example.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface XPDao {

    // --- Tasks Queries ---
    @Query("SELECT * FROM xp_tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<XPTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: XPTask)

    @Update
    suspend fun updateTask(task: XPTask)

    @Query("DELETE FROM xp_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)


    // --- Files Queries ---
    @Query("SELECT * FROM xp_files WHERE path = :path")
    fun getFilesByPath(path: String): Flow<List<XPFile>>

    @Query("SELECT * FROM xp_files")
    fun getAllFiles(): Flow<List<XPFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: XPFile)

    @Update
    suspend fun updateFile(file: XPFile)

    @Query("DELETE FROM xp_files WHERE id = :id")
    suspend fun deleteFileById(id: Int)


    // --- Installed Apps Queries ---
    @Query("SELECT * FROM xp_installed_apps")
    fun getAllApps(): Flow<List<XPDynamicApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: XPDynamicApp)

    @Query("UPDATE xp_installed_apps SET installed = :installed WHERE appId = :appId")
    suspend fun setAppInstalled(appId: String, installed: Boolean)
}
