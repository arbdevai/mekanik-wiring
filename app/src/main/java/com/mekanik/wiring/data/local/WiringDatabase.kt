package com.mekanik.wiring.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mekanik.wiring.data.local.dao.DiagramDao
import com.mekanik.wiring.data.local.entity.DiagramProjectEntity

@Database(
    entities = [DiagramProjectEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WiringDatabase : RoomDatabase() {

    abstract fun diagramDao(): DiagramDao

    companion object {
        @Volatile
        private var INSTANCE: WiringDatabase? = null

        fun getInstance(context: Context): WiringDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WiringDatabase::class.java,
                    "mekanik_wiring.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
