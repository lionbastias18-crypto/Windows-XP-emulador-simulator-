package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "xp_tasks")
data class XPTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val priority: String, // High, Medium, Low
    val completed: Boolean = false,
    val category: String, // Retro Office, Personal, Administrative
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "xp_files")
data class XPFile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val path: String, // e.g. "C:\\My Documents" or "C:\\Windows\\System32"
    val name: String,
    val content: String,
    val isDirectory: Boolean = false,
    val canRead: Boolean = true,
    val canWrite: Boolean = true,
    val canExecute: Boolean = true,
    val size: Long = content.length.toLong()
)

@Entity(tableName = "xp_installed_apps")
data class XPDynamicApp(
    @PrimaryKey val appId: String, // e.g., "buscaminas", "calculator", "xp_paint", "word_pad", "file_permissions"
    val appName: String,
    val installed: Boolean = false,
    val iconName: String
)
