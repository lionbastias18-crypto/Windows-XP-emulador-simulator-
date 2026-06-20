package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [XPTask::class, XPFile::class, XPDynamicApp::class], version = 1, exportSchema = false)
abstract class XPDatabase : RoomDatabase() {
    abstract fun xpDao(): XPDao

    companion object {
        @Volatile
        private var INSTANCE: XPDatabase? = null

        fun getDatabase(context: Context): XPDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    XPDatabase::class.java,
                    "xp_retro_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
