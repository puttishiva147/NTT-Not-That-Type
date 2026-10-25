package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UnderstoodItem::class], version = 1, exportSchema = false)
abstract class NttDatabase : RoomDatabase() {
    abstract fun understoodDao(): UnderstoodDao

    companion object {
        @Volatile
        private var INSTANCE: NttDatabase? = null

        fun getDatabase(context: Context): NttDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NttDatabase::class.java,
                    "ntt_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
