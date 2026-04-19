package com.example.funrun.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RunEntity::class], version = 1, exportSchema = false)
abstract class RunDatabase : RoomDatabase() {

    abstract fun runDao(): RunDao

    companion object {
        @Volatile
        private var INSTANCE: RunDatabase? = null

        fun getInstance(context: Context): RunDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    RunDatabase::class.java,
                    "run_database"
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}