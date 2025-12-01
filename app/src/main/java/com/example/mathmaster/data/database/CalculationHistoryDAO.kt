package com.example.mathmaster.database

import androidx.room.*
import com.example.mathmaster.model.CalculationHistory
import kotlinx.coroutines.flow.Flow
@Dao
interface CalculationHistoryDAO {
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CalculationHistory>>

    @Insert
    suspend fun insertHistory(history: CalculationHistory)

    @Delete
    suspend fun deleteHistory(history: CalculationHistory)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAllHistory()

    @Query("SELECT * FROM calculation_history WHERE id = :id")
    suspend fun getHistoryById(id: Long): CalculationHistory?

    @Query("SELECT * FROM calculation_history WHERE calculatorType = 'graph' ORDER BY timestamp DESC")
    fun getGraphHistory(): Flow<List<CalculationHistory>>

    @Query("DELETE FROM calculation_history WHERE calculatorType = 'graph'")
    suspend fun deleteGraphHistory()
}