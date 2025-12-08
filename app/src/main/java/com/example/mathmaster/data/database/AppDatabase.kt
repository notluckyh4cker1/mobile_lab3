package com.example.mathmaster.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.mathmaster.model.CalculationHistory

@Database(
    entities = [CalculationHistory::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calculationHistoryDao(): CalculationHistoryDAO

    companion object {
        fun getDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "mathmaster_database"
            ).fallbackToDestructiveMigration()
                .build()
        }
    }
}